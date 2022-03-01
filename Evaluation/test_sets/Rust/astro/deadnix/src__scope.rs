use crate::{binding::Binding, usage};
use rnix::{
    types::{AttrSet, EntryHolder, Ident, Lambda, LetIn, Pattern, TokenWrapper, TypedNode},
    NixLanguage, SyntaxKind,
};
use rowan::api::SyntaxNode;
use std::fmt;

/// AST subtree that declares variables
#[derive(Debug, Clone)]
pub enum Scope {
    LambdaPattern(Pattern, SyntaxNode<NixLanguage>),
    LambdaArg(Ident, SyntaxNode<NixLanguage>),
    LetIn(LetIn),
    RecAttrSet(AttrSet),
}

impl fmt::Display for Scope {
    fn fmt(&self, fmt: &mut fmt::Formatter) -> fmt::Result {
        match self {
            Scope::LambdaPattern(_, _) => write!(fmt, "lambda pattern"),
            Scope::LambdaArg(_, _) => write!(fmt, "lambda argument"),
            Scope::LetIn(_) => write!(fmt, "let binding"),
            Scope::RecAttrSet(_) => write!(fmt, "rec attrset"),
        }
    }
}

impl Scope {
    /// Construct a new Scope *if* this is an AST node that opens a new scope
    pub fn new(node: &SyntaxNode<NixLanguage>) -> Option<Self> {
        match node.kind() {
            SyntaxKind::NODE_LAMBDA => {
                let lambda = Lambda::cast(node.clone()).expect("Lambda::cast");
                let arg = lambda.arg().expect("lambda.arg()");
                let body = lambda.body().expect("lambda.body()");
                match arg.kind() {
                    SyntaxKind::NODE_IDENT => {
                        let name = Ident::cast(arg).expect("Ident::cast");
                        Some(Scope::LambdaArg(name, body))
                    }
                    SyntaxKind::NODE_PATTERN => {
                        let pattern = Pattern::cast(arg).expect("Pattern::cast");
                        Some(Scope::LambdaPattern(pattern, body))
                    }
                    _ => panic!("Unhandled arg kind: {:?}", arg.kind()),
                }
            }

            SyntaxKind::NODE_LET_IN => {
                let let_in = LetIn::cast(node.clone()).expect("LetIn::cast");
                Some(Scope::LetIn(let_in))
            }

            SyntaxKind::NODE_ATTR_SET => {
                let attr_set = AttrSet::cast(node.clone()).expect("AttrSet::cast");
                if attr_set.recursive() {
                    Some(Scope::RecAttrSet(attr_set))
                } else {
                    None
                }
            }

            _ => None,
        }
    }

    pub fn is_lambda_arg(&self) -> bool {
        matches!(self, Scope::LambdaArg(_, _))
    }

    pub fn is_lambda_pattern_name(&self, name: &Ident) -> bool {
        if let Scope::LambdaPattern(pattern, _) = self {
            pattern.entries().any(|entry|
                entry.name().expect("entry.name").as_str() == name.as_str()
            )
        } else {
            false
        }
    }

    /// The Bindings this Scope introduces
    pub fn bindings(&self) -> Box<dyn Iterator<Item = Binding>> {
        match self {
            Scope::LambdaPattern(pattern, _) => {
                let mortal = pattern.ellipsis();
                Box::new(
                    pattern
                        .at()
                        .map(|name| {
                            let binding_node = name.node().clone();
                            Binding::new(name, binding_node.clone(), binding_node, true)
                        })
                        .into_iter()
                        .chain(pattern.entries().map(move |entry| {
                            let name = entry.name().expect("entry.name");
                            Binding::new(name, entry.node().clone(), entry.node().clone(), mortal)
                        })),
                )
            }

            Scope::LambdaArg(name, _) => {
                let mortal = !name.as_str().starts_with('_');
                Box::new(Some(Binding::new(name.clone(), name.node().clone(), name.node().clone(), mortal)).into_iter())
            }

            Scope::LetIn(let_in) => Box::new(
                let_in
                    .inherits()
                    .flat_map(|inherit| {
                        let body_node = if let Some(from) = inherit.from() {
                            from.node().clone()
                        } else {
                            inherit.node().clone()
                        };
                        inherit
                            .idents()
                            .map(move |name| {
                                let name_node = name.node().clone();
                                Binding::new(name, body_node.clone(), name_node, true)
                            })
                    })
                    .chain(let_in.entries().map(|entry| {
                        let key = entry
                            .key()
                            .expect("entry.key")
                            .path()
                            .next()
                            .expect("key.path.next");
                        let name = Ident::cast(key).expect("Ident::cast");
                        Binding::new(name, entry.node().clone(), entry.node().clone(), true)
                    })),
            ),

            Scope::RecAttrSet(attr_set) => Box::new(
                attr_set
                    .inherits()
                    .flat_map(|inherit| {
                        let binding_node = inherit.node().clone();
                        inherit
                            .idents()
                            .map(move |name| {
                                let name_node = name.node().clone();
                                Binding::new(name, binding_node.clone(), name_node, false)
                            })
                    })
                    .chain(attr_set.entries().filter_map(|entry| {
                        let key = entry
                            .key()
                            .expect("entry.key")
                            .path()
                            .next()
                            .expect("key.path.next");
                        if key.kind() == SyntaxKind::NODE_IDENT {
                            let name = Ident::cast(key).expect("Ident::cast");
                            Some(Binding::new(name, entry.node().clone(), entry.node().clone(), false))
                        } else {
                            None
                        }
                    })),
            ),
        }
    }

    /// The code subtrees in which the introduced variables are available
    ///
    /// TODO: return `&SyntaxNode`
    pub fn bodies(&self) -> Box<dyn Iterator<Item = SyntaxNode<NixLanguage>>> {
        match self {
            Scope::LambdaPattern(pattern, body) => Box::new(
                pattern
                    .entries()
                    .map(|entry| entry.node().clone())
                    .chain(Some(body.clone()).into_iter()),
            ),

            Scope::LambdaArg(_, body) => Box::new(Some(body.clone()).into_iter()),

            Scope::LetIn(let_in) => Box::new(
                let_in
                    .inherits()
                    .filter_map(|inherit| inherit.from().map(|from| from.node().clone()))
                    .chain(let_in.entries().map(|entry| entry.node().clone()))
                    .chain(let_in.body()),
            ),

            Scope::RecAttrSet(attr_set) => Box::new(
                attr_set
                    .inherits()
                    .map(|inherit| inherit.node().clone())
                    .chain(attr_set.entries().map(|entry| entry.node().clone())),
            ),
        }
    }

    /// Check the `inherit (var) ...` and `inherit vars` clauses for a
    /// given `name`.
    ///
    /// Although a scope may shadow existing variable bindings, it can
    /// `inherit` bindings from the outer scope.
    pub fn inherits_from(&self, name: &Ident) -> bool {
        match self {
            Scope::LambdaPattern(_, _) | Scope::LambdaArg(_, _) => false,

            Scope::LetIn(let_in) => let_in.inherits().any(|inherit| {
                inherit.from().map_or_else(
                    || usage::find(name, inherit.node()),
                    |from| usage::find(name, from.node()),
                )
            }),

            Scope::RecAttrSet(attr_set) => attr_set.inherits().any(|inherit| {
                inherit.from().map_or_else(
                    || usage::find(name, inherit.node()),
                    |from| usage::find(name, from.node()),
                )
            }),
        }
    }
}
