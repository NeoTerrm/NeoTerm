extra-key: {
    version: 19
    with-default: true
    program: [ vim, vi, neovim ]

    key: [
        {
            code: "dd"
            with-enter: true
        },
        {
            code: ":x"
            with-enter: true
        },
        {
            code: ":w"
            with-enter: true
        },
        {
            code: ":q"
            with-enter: true
        }
    ]
}