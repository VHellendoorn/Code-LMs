/*
 * This file is part of the Nodle Chain distributed at https://github.com/NodleCode/chain
 * Copyright (C) 2022  Nodle International
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

#![cfg(test)]

use super::*;
use crate::{self as pallet_emergency_shutdown};
use frame_support::{assert_noop, assert_ok, ord_parameter_types, parameter_types};
use frame_system::{EnsureSignedBy, RawOrigin};
use sp_core::H256;
use sp_runtime::{
    testing::Header,
    traits::{BlakeTwo256, IdentityLookup},
    DispatchError::BadOrigin,
};

type UncheckedExtrinsic = frame_system::mocking::MockUncheckedExtrinsic<Test>;
type Block = frame_system::mocking::MockBlock<Test>;

frame_support::construct_runtime!(
    pub enum Test where
        Block = Block,
        NodeBlock = Block,
        UncheckedExtrinsic = UncheckedExtrinsic,
    {
        System: frame_system::{Pallet, Call, Config, Storage, Event<T>},
        TestModule: pallet_emergency_shutdown::{Pallet, Call, Storage, Event<T>},
    }
);

parameter_types! {
    pub const BlockHashCount: u64 = 250;
}
impl frame_system::Config for Test {
    type Origin = Origin;
    type Call = Call;
    type BlockWeights = ();
    type BlockLength = ();
    type SS58Prefix = ();
    type Index = u64;
    type BlockNumber = u64;
    type Hash = H256;
    type Hashing = BlakeTwo256;
    type AccountId = u64;
    type Lookup = IdentityLookup<Self::AccountId>;
    type Header = Header;
    type Event = ();
    type BlockHashCount = BlockHashCount;
    type Version = ();
    type PalletInfo = PalletInfo;
    type AccountData = ();
    type OnNewAccount = ();
    type OnKilledAccount = ();
    type DbWeight = ();
    type BaseCallFilter = frame_support::traits::Everything;
    type OnSetCode = ();
    type SystemWeightInfo = ();
}

ord_parameter_types! {
    pub const Admin: u64 = 1;
}
impl Config for Test {
    type Event = ();
    type ShutdownOrigin = EnsureSignedBy<Admin, u64>;
    type WeightInfo = ();
}

// This function basically just builds a genesis storage key/value store according to
// our desired mockup.
pub fn new_test_ext() -> sp_io::TestExternalities {
    frame_system::GenesisConfig::default()
        .build_storage::<Test>()
        .unwrap()
        .into()
}

#[test]
fn root_toggle() {
    new_test_ext().execute_with(|| {
        assert_ok!(TestModule::toggle(RawOrigin::Root.into()));
    })
}

#[test]
fn shutdown_origin_toggle() {
    new_test_ext().execute_with(|| {
        assert_ok!(TestModule::toggle(Origin::signed(Admin::get())));
    })
}

#[test]
fn toggle_on_off() {
    new_test_ext().execute_with(|| {
        assert_eq!(TestModule::shutdown(), false);

        assert_ok!(TestModule::toggle(RawOrigin::Root.into()));
        assert_eq!(TestModule::shutdown(), true);

        assert_ok!(TestModule::toggle(RawOrigin::Root.into()));
        assert_eq!(TestModule::shutdown(), false);
    })
}

#[test]
fn non_origin_fails() {
    new_test_ext().execute_with(|| {
        assert_noop!(TestModule::toggle(Origin::signed(0)), BadOrigin);
    })
}
