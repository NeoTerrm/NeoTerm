extra-key: {
    version: 20
    with-default: true
    program: [ vim, vi, nvim ]

    key: [
        {
            code: "<Esc> dd"
            display: "dd"
            with-enter: true
        },
        {
            code: "<Esc> :x"
            display: ":x"
            with-enter: true
        },
        {
            code: "<Esc> :w"
            display: ":w"
            with-enter: true
        },
        {
            code: "<Esc> :q"
            display: ":q"
            with-enter: true
        }
    ]
}