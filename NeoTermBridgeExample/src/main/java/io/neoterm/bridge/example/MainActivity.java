package io.neoterm.bridge.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import io.neoterm.bridge.Bridge;
import io.neoterm.bridge.SessionId;

public class MainActivity extends Activity {

    private static final int REQUEST_CODE_RUN = 1;
    private static final int REQUEST_CODE_APPEND = 2;
    private static final int REQUEST_CODE_APPEND_SILENTLY = 3;
    private static final String COMMAND = "echo \"hello world\"";

    private SessionId lastSessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onRunHelloWorld(View view) {
        Intent intent = Bridge.createExecuteIntent(COMMAND);
        startActivityForResult(intent, REQUEST_CODE_RUN);
    }

    public void onAppendHelloWorld(View view) {
        if (lastSessionId == null) {
            Toast.makeText(this, "Please run at least once",
                    Toast.LENGTH_SHORT).show();
        }

        Intent intent = Bridge.createExecuteIntent(lastSessionId, COMMAND);
        startActivityForResult(intent, REQUEST_CODE_APPEND);
    }

    public void onAppendHelloWorldSilently(View view) {
        if (lastSessionId == null) {
            Toast.makeText(this, "Please run at least once",
                    Toast.LENGTH_SHORT).show();
        }
        Intent intent = Bridge.createExecuteIntent(lastSessionId, COMMAND, false);
        startActivityForResult(intent, REQUEST_CODE_APPEND_SILENTLY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (requestCode) {
            case REQUEST_CODE_RUN:
                lastSessionId = Bridge.parseResult(data);
                break;
            case REQUEST_CODE_APPEND:
                Toast.makeText(this, "appended to " + lastSessionId.toString(), Toast.LENGTH_SHORT).show();
                break;
            case REQUEST_CODE_APPEND_SILENTLY:
                Toast.makeText(this, "appended silently to " + lastSessionId.toString(), Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
