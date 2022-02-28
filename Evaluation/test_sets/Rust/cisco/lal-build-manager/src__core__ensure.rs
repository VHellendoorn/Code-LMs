use std::path::Path;
use std::fs;
use std::io;


/// Ensure a directory exists and is empty
pub fn ensure_dir_exists_fresh(dir: &str) -> io::Result<()> {
    let dir = Path::new(dir);
    if dir.is_dir() {
        // clean it out first
        fs::remove_dir_all(&dir)?;
    }
    fs::create_dir_all(&dir)?;
    Ok(())
}
