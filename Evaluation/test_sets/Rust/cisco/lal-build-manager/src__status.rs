use ansi_term::{Colour, ANSIString};
use core::input;
use super::{Lockfile, CliError, LalResult, Manifest};

fn version_string(lf: Option<&Lockfile>, show_ver: bool, show_time: bool) -> ANSIString<'static> {
    if let Some(lock) = lf {
        let ver_color = if lock.version.parse::<u32>().is_ok() { 12 } else { 11 };
        let verstr = Colour::Fixed(ver_color)
            .paint(format!("({}-{})", lock.version, lock.environment.clone()));
        let timestr = if let Some(ref time) = lock.built {
            Colour::Fixed(14).paint(format!("({})", time))
        } else {
            ANSIString::from("")
        };
        if !show_ver && !show_time {
            ANSIString::from("")
        } else if show_ver && !show_time {
            verstr
        } else if !show_ver && show_time {
            timestr
        } else {
            ANSIString::from(format!("{} {}", verstr, timestr))
        }
    } else {
        ANSIString::from("")
    }
}

fn status_recurse(
    dep: &str,
    lf: &Lockfile,
    n: usize,
    parent_indent: Vec<bool>,
    show_ver: bool,
    show_time: bool,
) {
    assert_eq!(dep, &lf.name);
    let len = lf.dependencies.len();
    for (i, (k, sublock)) in lf.dependencies.iter().enumerate() {
        let has_children = !sublock.dependencies.is_empty();
        let fork_char = if has_children { "┬" } else { "─" };
        let is_last = i == len - 1;
        let turn_char = if is_last { "└" } else { "├" };

        let ws: String = parent_indent.iter().fold(String::new(), |res, &ws_only| {
            res + (if ws_only { "  " } else { "│ " })
        });

        println!("│ {}{}─{} {} {}",
                 ws,
                 turn_char,
                 fork_char,
                 k,
                 version_string(Some(sublock), show_ver, show_time));

        let mut next_indent = parent_indent.clone();
        next_indent.push(is_last);

        status_recurse(k, sublock, n + 1, next_indent, show_ver, show_time);
    }
}

/// Prints a fancy dependency tree of `./INPUT` to stdout.
///
/// This is the quick version information of what you currently have in `./INPUT`.
/// It prints the tree and highlights versions, as well as both missing and extraneous
/// dependencies in `./INPUT`.
///
/// If the full flag is given, then the full dependency tree is also spliced in
/// from lockfile data.
///
/// It is not intended as a verifier, but will nevertheless produce a summary at the end.
pub fn status(manifest: &Manifest, full: bool, show_ver: bool, show_time: bool) -> LalResult<()> {
    let mut error = None;

    let lf = Lockfile::default().populate_from_input()?;

    println!("{}", manifest.name);
    let deps = input::analyze_full(manifest)?;
    let len = deps.len();
    for (i, (d, dep)) in deps.iter().enumerate() {
        let notes = if dep.missing && !dep.development {
            error = Some(CliError::MissingDependencies);
            Colour::Red.paint("(missing)").to_string()
        } else if dep.missing {
            Colour::Yellow.paint("(missing)").to_string()
        } else if dep.development {
            "(dev)".to_string()
        } else if dep.extraneous {
            error = Some(CliError::ExtraneousDependencies(dep.name.clone()));
            Colour::Green.paint("(extraneous)").to_string()
        } else {
            "".to_string()
        };
        // list children in --full mode
        // NB: missing deps will not be populatable
        let has_children = full && !dep.missing &&
            !&lf.dependencies[&dep.name].dependencies.is_empty();
        let fork_char = if has_children { "┬" } else { "─" };
        let is_last = i == len - 1;
        let turn_char = if is_last { "└" } else { "├" };

        // first level deps are formatted with more metadata
        let level1 = format!("{} {}", d, notes);
        let ver_str = version_string(lf.dependencies.get(&dep.name), show_ver, show_time);
        println!("{}─{} {} {}", turn_char, fork_char, level1, ver_str);

        if has_children {
            trace!("Attempting to get {} out of lockfile deps {:?}",
                   dep.name,
                   lf.dependencies);
            // dep unwrap relies on populate_from_input try! reading all lockfiles earlier
            let sub_lock = &lf.dependencies[&dep.name];
            status_recurse(&dep.name, sub_lock, 1, vec![], show_ver, show_time);
        }
    }

    // Return one of the errors as the main one (no need to vectorize these..)
    if let Some(e) = error {
        return Err(e);
    }
    Ok(())
}
