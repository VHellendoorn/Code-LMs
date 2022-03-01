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

#![cfg_attr(not(feature = "std"), no_std)]

use crate::{constants, Balances, Call, Event, Origin, OriginCaller, Runtime};

use frame_support::{parameter_types, traits::EqualPrivilegeOnly, weights::Weight};
use primitives::{AccountId, Balance};
use sp_runtime::Perbill;

impl pallet_utility::Config for Runtime {
    type Event = Event;
    type Call = Call;
    type PalletsOrigin = OriginCaller;
    type WeightInfo = ();
}

parameter_types! {
    // One storage item; key size is 32; value is size 4+4+16+32 bytes = 56 bytes.
    pub const DepositBase: Balance = constants::deposit(1, 88);
    // Additional storage item size of 32 bytes.
    pub const DepositFactor: Balance = constants::deposit(0, 32);
    pub const MaxSignatories: u16 = 100;
}

impl pallet_multisig::Config for Runtime {
    type Event = Event;
    type Call = Call;
    type Currency = Balances;
    type DepositBase = DepositBase;
    type DepositFactor = DepositFactor;
    type MaxSignatories = MaxSignatories;
    type WeightInfo = pallet_multisig::weights::SubstrateWeight<Runtime>;
}

impl pallet_randomness_collective_flip::Config for Runtime {}

parameter_types! {
    pub MaximumSchedulerWeight: Weight = Perbill::from_percent(80) *
        constants::RuntimeBlockWeights::get().max_block;
    pub const MaxScheduledPerBlock: u32 = 50;
}

impl pallet_scheduler::Config for Runtime {
    type Event = Event;
    type Origin = Origin;
    type PalletsOrigin = OriginCaller;
    type Call = Call;
    type MaximumWeight = MaximumSchedulerWeight;
    type ScheduleOrigin = frame_system::EnsureRoot<AccountId>;
    type OriginPrivilegeCmp = EqualPrivilegeOnly;
    type MaxScheduledPerBlock = MaxScheduledPerBlock;
    type WeightInfo = pallet_scheduler::weights::SubstrateWeight<Runtime>;
}
