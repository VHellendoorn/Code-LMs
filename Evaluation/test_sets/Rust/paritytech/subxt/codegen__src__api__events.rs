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
    PalletEventMetadata,
    PalletMetadata,
};
use proc_macro2::TokenStream as TokenStream2;
use quote::quote;
use scale_info::form::PortableForm;

pub fn generate_events(
    type_gen: &TypeGenerator,
    pallet: &PalletMetadata<PortableForm>,
    event: &PalletEventMetadata<PortableForm>,
    types_mod_ident: &syn::Ident,
) -> TokenStream2 {
    let struct_defs = super::generate_structs_from_variants(
        type_gen,
        event.ty.id(),
        |name| name.into(),
        "Event",
    );
    let event_structs = struct_defs.iter().map(|struct_def| {
        let pallet_name = &pallet.name;
        let event_struct = &struct_def.name;
        let event_name = struct_def.name.to_string();

        quote! {
            #struct_def

            impl ::subxt::Event for #event_struct {
                const PALLET: &'static str = #pallet_name;
                const EVENT: &'static str = #event_name;
            }
        }
    });
    let event_type = type_gen.resolve_type_path(event.ty.id(), &[]);

    quote! {
        pub type Event = #event_type;
        pub mod events {
            use super::#types_mod_ident;
            #( #event_structs )*
        }
    }
}
