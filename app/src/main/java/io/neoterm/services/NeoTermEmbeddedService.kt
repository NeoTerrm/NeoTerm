package io.neoterm.services

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.RemoteException
import android.os.ResultReceiver
import android.text.TextUtils
import android.util.ArrayMap
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout

import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.frontend.config.NeoPreference
import io.neoterm.frontend.session.shell.ShellParameter
import io.neoterm.frontend.terminal.TerminalView

class NeoTermEmbeddedService : Service() {
    private var mTermuxService: NeoTermService? = null
    private val mSessions = ArrayMap<String, TerminalSession>()
    private val mResultReceivers = ArrayMap<String, ResultReceiver>()
    private var mTerminalView: TerminalView? = null
    private var mRootView: RootView? = null
    private var mWindowManager: WindowManager? = null

    private var mHandler: Handler? = null
    private val mLock = Any()

    override fun onCreate() {
        super.onCreate()
        bindService(Intent(this, NeoTermService::class.java), mServiceConnection, Context.BIND_AUTO_CREATE)
        mHandler = Handler()
        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        val sessionName = intent.getStringExtra("sessionName")
        if (!TextUtils.isEmpty(sessionName)) {
            destroySession(sessionName)
        }
        return super.onUnbind(intent)
    }

    private fun makeSureViewExit() {
        if (mRootView == null) {
            mRootView = RootView(this)
            val params = WindowManager.LayoutParams()
            params.type = WindowManager.LayoutParams.TYPE_PHONE
            params.width = 800
            params.height = 800
            params.gravity = Gravity.START or Gravity.TOP
            params.flags = params.flags or (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            mWindowManager!!.addView(mRootView, params)
        }
        if (mTerminalView == null) {
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            mTerminalView = inflater.inflate(R.layout.ui_term_embedded, mRootView, false) as TerminalView
            mTerminalView!!.textSize = NeoPreference.getFontSize()
            mRootView!!.addView(mTerminalView)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
        if (mSessions.size > 0) {
            mSessions.forEach {
                it.value.finishIfRunning();
            }
        }
        mSessions.clear();
        mResultReceivers.clear();
    }

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mTermuxService = (service as NeoTermService.NeoTermBinder).service
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mTermuxService = null
        }
    }

    private fun createNewSession(sessionName: String?): TerminalSession {
        val terminalSession = mTermuxService!!.createTermSession(ShellParameter().systemShell(false))
        if (sessionName != null) {
            terminalSession.mSessionName = sessionName
        }
        return terminalSession
    }

    private fun findSession(sessionName: String): TerminalSession? {
        return mSessions[sessionName]
    }

    // called from main thread
    private fun attachSessionAndVisible(session: TerminalSession) {
        mTerminalView!!.attachSession(session)
        mRootView!!.visibility = View.VISIBLE
    }

    private fun onSessionDestroyed(session: TerminalSession) {
        synchronized(mLock) {
            mTermuxService!!.removeTermSession(session)
            if (mSessions.size == 0) {
                // no session now
                if (mRootView != null) {
                    mWindowManager!!.removeView(mRootView)
                }
                stopSelf()
            }
        }
    }

    private fun layoutView(x: Int, y: Int, width: Int, height: Int) {
        if (mRootView != null) {
            val params = mRootView!!.layoutParams as WindowManager.LayoutParams
            params.width = width
            params.height = height
            mRootView!!.layoutParams = params
            params.x = x
            params.y = y
            mWindowManager!!.updateViewLayout(mRootView, params)
        }
    }

    private val mBinder = object : INeoTermEmbedded.Stub() {
        @Throws(RemoteException::class)
        override fun writeToTerminal(sessionName: String, command: String) {
            mHandler!!.post { mTerminalView!!.currentSession.write(command) }
        }

        @Throws(RemoteException::class)
        override fun onCreate(sessionName: String, resultReceiver: ResultReceiver) {
            synchronized(mLock) {
                if (mSessions[sessionName] != null) {
                    throw RemoteException("sessionName has been created")
                }
            }
            mHandler!!.post {
                synchronized(mLock) {
                    val session = createNewSession(sessionName)
                    mSessions.put(sessionName, session)
                    mResultReceivers.put(sessionName, resultReceiver)
                }
            }
        }

        @Throws(RemoteException::class)
        override fun onVisible(sessionName: String) {
            synchronized(mLock) {
                val session = findSession(sessionName)
                mHandler!!.post {
                    makeSureViewExit()
                    attachSessionAndVisible(session!!)
                }
            }
        }

        @Throws(RemoteException::class)
        override fun layoutView(sessionName: String, x: Int, y: Int, width: Int, height: Int) {
            mHandler!!.post { this@NeoTermEmbeddedService.layoutView(x, y, width, height) }
        }

        @Throws(RemoteException::class)
        override fun onInVisible(sessionName: String) {
            synchronized(mLock) {
                val session = findSession(sessionName)
                mHandler!!.post { mRootView!!.visibility = View.GONE }
            }
        }

        @Throws(RemoteException::class)
        override fun onDestroyed(sessionName: String) {
            destroySession(sessionName)
        }
    }

    private fun destroySession(sessionName: String) {
        synchronized(mLock) {
            val session = findSession(sessionName) ?: return
            mSessions.remove(sessionName)
            mResultReceivers.remove(sessionName)
            mHandler!!.post { onSessionDestroyed(session) }
        }
    }

    // just like rootView in activity
    private inner class RootView(context: Context) : FrameLayout(context) {

        init {
            requestFocus()
            isFocusable = true
            isFocusableInTouchMode = false
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            return false
        }
    }
}