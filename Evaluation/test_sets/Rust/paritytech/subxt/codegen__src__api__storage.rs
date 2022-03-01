// Copyright 2019-2022 Parity Technologies (UK) Ltd.
// This file is part of subxt.
//
// subxt is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// subxt is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with subxt.  If not, see <http://www.gnu.org/licenses/>.

use crate::types::TypeGenerator;
use frame_metadata::{
    PalletMetadata,
    PalletStorageMetadata,
    StorageEntryMetadata,
    StorageEntryModifier,
    StorageEntryType,
    StorageHasher,
};
use heck::SnakeCase as _;
use proc_macro2::TokenStream as TokenStream2;
use proc_macro_error::abort_call_site;
use quote::{
    format_ident,
    quote,
};
use scale_info::{
    form::PortableForm,
    TypeDef,
};

pub fn generate_storage(
    type_gen: &TypeGenerator,
    pallet: &PalletMetadata<PortableForm>,
    storage: &PalletStorageMetadata<PortableForm>,
    types_mod_ident: &syn::Ident,
) -> TokenStream2 {
    let (storage_structs, storage_fns): (Vec<_>, Vec<_>) = storage
        .entries
        .iter()
        .map(|entry| generate_storage_entry_fns(type_gen, pallet, entry))
        .unzip();

    quote! {
        pub mod storage {
            use super::#types_mod_ident;

            #( #storage_structs )*

            pub struct StorageApi<'a, T: ::subxt::Config> {
                client: &'a ::subxt::Client<T>,
            }

            impl<'a, T: ::subxt::Config> StorageApi<'a, T> {
                pub fn new(client: &'a ::subxt::Client<T>) -> Self {
                    Self { client }
                }

                #( #storage_fns )*
            }
        }
    }
}

fn generate_storage_entry_fns(
    type_gen: &TypeGenerator,
    pallet: &PalletMetadata<PortableForm>,
    storage_entry: &StorageEntryMetadata<PortableForm>,
) -> (TokenStream2, TokenStream2) {
    let entry_struct_ident = format_ident!("{}", storage_entry.name);
    let (fields, entry_struct, constructor, key_impl) = match storage_entry.ty {
        StorageEntryType::Plain(_) => {
            let entry_struct = quote!( pub struct #entry_struct_ident; );
            let constructor = quote!( #entry_struct_ident );
            let key_impl = quote!(::subxt::StorageEntryKey::Plain);
            (vec![], entry_struct, constructor, key_impl)
        }
        StorageEntryType::Map {
            ref key,
            ref hashers,
            ..
        } => {
            let key_ty = type_gen.resolve_type(key.id());
            let hashers = hashers
                .iter()
                .map(|hasher| {
                    let hasher = match hasher {
                        StorageHasher::Blake2_128 => "Blake2_128",
                        StorageHasher::Blake2_256 => "Blake2_256",
                        StorageHasher::Blake2_128Concat => "Blake2_128Concat",
                        StorageHasher::Twox128 => "Twox128",
                        StorageHasher::Twox256 => "Twox256",
                        StorageHasher::Twox64Concat => "Twox64Concat",
                        StorageHasher::Identity => "Identity",
                    };
                    let hasher = format_ident!("{}", hasher);
                    quote!( ::subxt::StorageHasher::#hasher )
                })
                .collect::<Vec<_>>();
            match key_ty.type_def() {
                TypeDef::Tuple(tuple) => {
                    let fields = tuple
                        .fields()
                        .iter()
                        .enumerate()
                        .map(|(i, f)| {
                            let field_name = format_ident!("_{}", syn::Index::from(i));
                            let field_type = type_gen.resolve_type_path(f.id(), &[]);
                            (field_name, field_type)
                        })
                        .collect::<Vec<_>>();
                    // toddo: [AJ] use unzip here?
                    let tuple_struct_fields =
                        fields.iter().map(|(_, field_type)| field_type);
                    let field_names = fields.iter().map(|(field_name, _)| field_name);
                    let entry_struct = quote! {
                        pub struct #entry_struct_ident( #( pub #tuple_struct_fields ),* );
                    };
                    let constructor =
                        quote!( #entry_struct_ident( #( #field_names ),* ) );
                    let keys = (0..tuple.fields().len()).into_iter().zip(hashers).map(
                        |(field, hasher)| {
                            let index = syn::Index::from(field);
                            quote!( ::subxt::StorageMapKey::new(&self.#index, #hasher) )
                        },
                    );
                    let key_impl = quote! {
                        ::subxt::StorageEntryKey::Map(
                            vec![ #( #keys ),* ]
                        )
                    };
                    (fields, entry_struct, constructor, key_impl)
                }
                _ => {
                    let ty_path = type_gen.resolve_type_path(key.id(), &[]);
                    let fields = vec![(format_ident!("_0"), ty_path.clone())];
                    let entry_struct = quote! {
                        pub struct #entry_struct_ident( pub #ty_path );
                    };
                    let constructor = quote!( #entry_struct_ident(_0) );
                    let hasher = hashers.get(0).unwrap_or_else(|| {
                        abort_call_site!("No hasher found for single key")
                    });
                    let key_impl = quote! {
                        ::subxt::StorageEntryKey::Map(
                            vec![ ::subxt::StorageMapKey::new(&self.0, #hasher) ]
                        )
                    };
                    (fields, entry_struct, constructor, key_impl)
                }
            }
        }
    };
    let pallet_name = &pallet.name;
    let storage_name = &storage_entry.name;
    let fn_name = format_ident!("{}", storage_entry.name.to_snake_case());
    let fn_name_iter = format_ident!("{}_iter", fn_name);
    let storage_entry_ty = match storage_entry.ty {
        StorageEntryType::Plain(ref ty) => ty,
        StorageEntryType::Map { ref value, .. } => value,
    };
    let storage_entry_value_ty = type_gen.resolve_type_path(storage_entry_ty.id(), &[]);
    let (return_ty, fetch) = match storage_entry.modifier {
        StorageEntryModifier::Default => {
            (quote!( #storage_entry_value_ty ), quote!(fetch_or_default))
        }
        StorageEntryModifier::Optional => {
            (
                quote!( ::core::option::Option<#storage_entry_value_ty> ),
                quote!(fetch),
            )
        }
    };

    let storage_entry_type = quote! {
        #entry_struct

        impl ::subxt::StorageEntry for #entry_struct_ident {
            const PALLET: &'static str = #pallet_name;
            const STORAGE: &'static str = #storage_name;
            type Value = #storage_entry_value_ty;
            fn key(&self) -> ::subxt::StorageEntryKey {
                #key_impl
            }
        }
    };

    let client_iter_fn = if matches!(storage_entry.ty, StorageEntryType::Map { .. }) {
        quote! (
            pub async fn #fn_name_iter(
                &self,
                hash: ::core::option::Option<T::Hash>,
            ) -> ::core::result::Result<::subxt::KeyIter<'a, T, #entry_struct_ident>, ::subxt::BasicError> {
                self.client.storage().iter(hash).await
            }
        )
    } else {
        quote!()
    };

    let key_args = fields
        .iter()
        .map(|(field_name, field_type)| quote!( #field_name: #field_type ));
    let client_fns = quote! {
        pub async fn #fn_name(
            &self,
            #( #key_args, )*
            hash: ::core::option::Option<T::Hash>,
        ) -> ::core::result::Result<#return_ty, ::subxt::BasicError> {
            let entry = #constructor;
            self.client.storage().#fetch(&entry, hash).await
        }

        #client_iter_fn
    };

    (storage_entry_type, client_fns)
}
