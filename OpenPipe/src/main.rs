use std::env;
use std::ptr;
use winapi::um::winnt::HANDLE;
use winapi::um::winbase::{
    CreateNamedPipeA, PIPE_ACCESS_DUPLEX, PIPE_READMODE_MESSAGE,
    PIPE_TYPE_MESSAGE, PIPE_UNLIMITED_INSTANCES, PIPE_WAIT
};

fn main() {
    let args: Vec<String> = env::args().collect();
    if args.len() != 2 {
        eprintln!("Usage: openpipe.exe <pipe_name>");
        return;
    }

    let pipe_name = format!(r"\\.\pipe\{}", args[1]);
    let pipe_name_cstr = std::ffi::CString::new(pipe_name.as_str()).expect("CString::new failed");

    let h_pipe: HANDLE = unsafe {
        CreateNamedPipeA(
            pipe_name_cstr.as_ptr(),
            PIPE_ACCESS_DUPLEX,
            PIPE_TYPE_MESSAGE | PIPE_READMODE_MESSAGE | PIPE_WAIT,
            PIPE_UNLIMITED_INSTANCES,
            512, // Output buffer size
            512, // Input buffer size
            0,   // Client time-out
            ptr::null_mut(), // Default security attributes
        )
    };

    if h_pipe.is_null() {
        eprintln!("Failed to create named pipe: {}", unsafe { winapi::um::errhandlingapi::GetLastError() });
        return;
    } else {
        println!("Named pipe created: {}", &pipe_name);
    }

    std::thread::park();
}