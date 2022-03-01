use itertools::Itertools;

#[derive(Debug, thiserror::Error)]
#[error("Found following errors during validation phase: {}", validation_errors(.0))]
pub struct ValidationErrors(Vec<ValidationError>);

impl ValidationErrors {
    pub fn new(errors: Vec<ValidationError>) -> Self {
        Self(errors)
    }
}

fn validation_errors(errors: &[ValidationError]) -> String {
    errors.iter().map(|s| format!("* {}", s)).join("\n")
}

#[derive(Debug, thiserror::Error)]
pub enum ValidationError {
    #[error(
        "Service '{before}', should start after '{after}', but there is no service with such name."
    )]
    MissingDependency { before: String, after: String },
    #[error("Command is defined, but it is empty for service: {service}")]
    CommandEmpty { service: String },
}
