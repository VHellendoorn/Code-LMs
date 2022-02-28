use serde_json;
use chrono::UTC;
use rand;

use std::path::{Path, PathBuf};
use std::fs::File;
use std::io::prelude::*;

use std::collections::{HashMap, BTreeMap};
use std::collections::BTreeSet;
use std::fmt;

use super::{CliError, LalResult, input};

/// Representation of a docker container image
#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct Container {
    /// The fully qualified image name
    pub name: String,
    /// The tag to use
    pub tag: String,
}

impl Container {
    /// Container struct with latest tag
    pub fn latest(name: &str) -> Self {
        Container {
            name: name.into(),
            tag: "latest".into(),
        }
    }
}

impl fmt::Display for Container {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result { write!(f, "{}:{}", self.name, self.tag) }
}

/// Convenience default for functions that require Lockfile inspection
/// Intentionally kept distinct from normal build images
impl Default for Container {
    fn default() -> Self {
        Container {
            name: "ubuntu".into(),
            tag: "xenial".into(),
        }
    }
}

impl Container {
    /// Initialize a container struct
    ///
    /// This will split the container on `:` to actually fetch the tag, and if no tag
    /// was present, it will assume tag is latest as per docker conventions.
    pub fn new(container: &str) -> Container {
        let split: Vec<&str> = container.split(':').collect();
        let tag = if split.len() == 2 { split[1] } else { "latest" };
        let cname = if split.len() == 2 { split[0] } else { container };
        Container {
            name: cname.into(),
            tag: tag.into(),
        }
    }
}

/// Representation of `lockfile.json`
#[allow(non_snake_case)]
#[derive(Serialize, Deserialize, Debug)]
pub struct Lockfile {
    /// Name of the component built
    pub name: String,
    /// Build configuration used
    pub config: String,
    /// Container and tag used to build
    pub container: Container,
    /// Name of the environment for the container at the time
    pub environment: String,
    /// Name of the default environment set in the manifest
    pub defaultEnv: Option<String>,
    /// Revision id from version control
    pub sha: Option<String>,
    /// Version of the component built
    pub version: String,
    /// Version of the lal tool
    pub tool: String,
    /// Built timestamp
    pub built: Option<String>,
    /// Recursive map of dependencies used
    pub dependencies: BTreeMap<String, Lockfile>,
}

/// Generates a temporary empty lockfile for internal analysis
impl Default for Lockfile {
    fn default() -> Self { Lockfile::new("templock", &Container::default(), "none", None, None) }
}

impl Lockfile {
    /// Initialize an empty Lockfile with defaults
    ///
    /// If no version is given, the version is EXPERIMENTAL-{randhex} for Colony.
    pub fn new(
        name: &str,
        container: &Container,
        env: &str,
        v: Option<String>,
        build_cfg: Option<&str>,
    ) -> Self {
        let def_version = format!("EXPERIMENTAL-{:x}", rand::random::<u64>());
        let time = UTC::now();
        Lockfile {
            name: name.to_string(),
            version: v.unwrap_or(def_version),
            config: build_cfg.unwrap_or("release").to_string(),
            container: container.clone(),
            tool: env!("CARGO_PKG_VERSION").to_string(),
            built: Some(time.format("%Y-%m-%d %H:%M:%S").to_string()),
            defaultEnv: Some(env.into()),
            environment: env.into(),
            dependencies: BTreeMap::new(),
            sha: None,
        }
    }

    /// Opened lockfile at a path
    pub fn from_path(lock_path: &PathBuf, name: &str) -> LalResult<Self> {
        if !lock_path.exists() {
            return Err(CliError::MissingLockfile(name.to_string()));
        }
        let mut lock_str = String::new();
        File::open(lock_path)?.read_to_string(&mut lock_str)?;
        Ok(serde_json::from_str(&lock_str)?)
    }

    /// A reader from ARTIFACT directory
    pub fn release_build() -> LalResult<Self> {
        let lpath = Path::new("ARTIFACT").join("lockfile.json");
        Ok(Lockfile::from_path(&lpath, "release build")?)
    }

    // Helper constructor for input populator below
    fn from_input_component(component: &str) -> LalResult<Self> {
        let lock_path = Path::new("./INPUT").join(component).join("lockfile.json");
        Ok(Lockfile::from_path(&lock_path, component)?)
    }


    /// Read all the lockfiles in INPUT to generate the full lockfile
    ///
    /// NB: This currently reads all the lockfiles partially in `analyze`,
    /// the re-reads them fully in `read_lockfile_from_component` so can be sped up.
    pub fn populate_from_input(mut self) -> LalResult<Self> {
        debug!("Reading all lockfiles");
        let deps = input::analyze()?;
        for name in deps.keys() {
            trace!("Populating lockfile with {}", name);
            let deplock = Lockfile::from_input_component(name)?;
            self.dependencies.insert(name.clone(), deplock);
        }
        Ok(self)
    }

    /// Attach a default environment to the lockfile
    pub fn set_default_env(mut self, default: String) -> Self {
        self.defaultEnv = Some(default);
        self
    }

    /// Attach a revision id from source control
    pub fn attach_revision_id(mut self, sha: Option<String>) -> Self {
        self.sha = sha;
        self
    }

    /// Attach a name to the lockfile
    pub fn set_name(mut self, name: &str) -> Self {
        self.name = name.into();
        self
    }

    /// Write the current `Lockfile` struct to a Path
    pub fn write(&self, pth: &Path) -> LalResult<()> {
        let encoded = serde_json::to_string_pretty(self)?;
        let mut f = File::create(pth)?;
        write!(f, "{}\n", encoded)?;
        debug!("Wrote lockfile {}: \n{}", pth.display(), encoded);
        Ok(())
    }
}


// name of component -> (value1, value2, ..)
pub type ValueUsage = HashMap<String, BTreeSet<String>>;

// The hardcore dependency analysis parts
impl Lockfile {
    // helper to extract specific keys out of a struct
    fn get_value(&self, key: &str) -> String {
        if key == "version" {
            self.version.clone()
        } else if key == "environment" {
            self.environment.clone()
        } else {
            unreachable!("Only using get_value internally");
        }
    }

    /// Recursive function to check for multiple version/environment (key) use
    fn find_all_values(&self, key: &str) -> ValueUsage {
        let mut acc = HashMap::new();
        // for each entry in dependencies
        for (main_name, dep) in &self.dependencies {
            // Store the dependency
            if !acc.contains_key(main_name) {
                acc.insert(main_name.clone(), BTreeSet::new());
            }
            {
                // Only borrow as mutable once - so creating a temporary scope
                let first_value_set = acc.get_mut(main_name).unwrap();
                first_value_set.insert(dep.get_value(key));
            }

            // Recurse into its dependencies
            trace!("Recursing into deps for {}, acc is {:?}", main_name, acc);
            for (name, value_set) in dep.find_all_values(key) {
                trace!("Found {} for for {} under {} as {:?}",
                       key,
                       name,
                       main_name,
                       value_set);
                // ensure each entry from above exists in current accumulator
                if !acc.contains_key(&name) {
                    acc.insert(name.clone(), BTreeSet::new());
                }
                // union the entry of value for the current name
                let full_value_set = acc.get_mut(&name).unwrap(); // know this exists now
                for value in value_set {
                    full_value_set.insert(value);
                }
            }
        }
        acc
    }

    /// List all used versions used of each dependency
    pub fn find_all_dependency_versions(&self) -> ValueUsage { self.find_all_values("version") }

    /// List all used environments used of each dependency
    pub fn find_all_environments(&self) -> ValueUsage { self.find_all_values("environment") }

    /// List all dependency names used by each dependency (not transitively)
    pub fn find_all_dependency_names(&self) -> ValueUsage {
        let mut acc = HashMap::new();
        // ensure root node exists
        acc.entry(self.name.clone())
            .or_insert_with(|| self.dependencies.keys().cloned().collect());
        for dep in self.dependencies.values() {
            // recurse and merge into parent acc:
            for (n, d) in dep.find_all_dependency_names() {
                acc.entry(n).or_insert(d);
            }
        }
        acc
    }
}

/// Reverse dependency methods
///
/// Similar to the above ones - requires a populated lockfile to make sense.
impl Lockfile {
    /// List all dependees for each dependency
    pub fn get_reverse_deps(&self) -> ValueUsage {
        let mut acc = HashMap::new();
        // ensure the root node exists (matters for first iteration)
        if !acc.contains_key(&self.name) {
            // don't expand the tree further outside self
            acc.insert(self.name.clone(), BTreeSet::new());
        }

        // for each entry in dependencies
        for (main_name, dep) in &self.dependencies {
            // ensure each entry from above exists in current accumulator
            if !acc.contains_key(&dep.name) {
                acc.insert(dep.name.clone(), BTreeSet::new());
            }
            {
                // Only borrow as mutable once - so creating a temporary scope
                let first_value_set = acc.get_mut(&dep.name).unwrap();
                first_value_set.insert(self.name.clone());
            }

            // Recurse into its dependencies
            trace!("Recursing into deps for {}, acc is {:?}", main_name, acc);

            // merge results recursively
            for (name, value_set) in dep.get_reverse_deps() {
                trace!("Found revdeps for {} as {:?}", name, value_set);
                // if we don't already have new entries, add them:
                if !acc.contains_key(&name) {
                    acc.insert(name.clone(), BTreeSet::new()); // blank first
                }
                // merge in values from recursion
                let full_value_set = acc.get_mut(&name).unwrap(); // know this exists now
                // union in values from recursion
                for value in value_set {
                    full_value_set.insert(value);
                }
            }
        }
        acc
    }

    /// List all dependees for a dependency transitively
    pub fn get_reverse_deps_transitively_for(&self, component: String) -> BTreeSet<String> {
        let revdeps = self.get_reverse_deps();
        trace!("Got rev deps: {:?}", revdeps);
        let mut res = BTreeSet::new();

        if !revdeps.contains_key(&component) {
            warn!("Could not find {} in the dependency tree for {}",
                  component,
                  self.name);
            return res;
        }

        let mut current_cycle = vec![component];
        while !current_cycle.is_empty() {
            let mut next_cycle = vec![];
            for name in current_cycle {
                // get revdeps for it (must exist by construction)
                for dep in &revdeps[&name] {
                    res.insert(dep.clone());
                    next_cycle.push(dep.clone());
                }
            }
            current_cycle = next_cycle;
        }
        res
    }
}
