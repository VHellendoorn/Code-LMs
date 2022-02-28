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

//! An amendment module instance manages amendments to the chain. There could be a security
//! delay configured along with a veto capability.
mod benchmarking;

#[cfg(test)]
mod tests;

use frame_support::{
    traits::{schedule::DispatchTime::At, schedule::Named as ScheduleNamed, LockIdentifier},
    weights::GetDispatchInfo,
};
use frame_system::{self as system};
use sp_runtime::traits::Dispatchable;
use sp_std::prelude::Box;

const AMENDMENTS_ID: LockIdentifier = *b"amendmen";

pub mod weights;
pub use weights::WeightInfo;

pub use pallet::*;

#[frame_support::pallet]
pub mod pallet {
    use super::*;
    use frame_support::pallet_prelude::*;
    use frame_system::pallet_prelude::*;

    #[pallet::config]
    pub trait Config: frame_system::Config {
        type Event: From<Event<Self>> + IsType<<Self as frame_system::Config>::Event>;

        type Amendment: Parameter
            + Dispatchable<Origin = Self::Origin>
            + From<frame_system::Call<Self>>
            + GetDispatchInfo;

        type Scheduler: ScheduleNamed<Self::BlockNumber, Self::Amendment, Self::PalletsOrigin>;
        type PalletsOrigin: From<frame_system::RawOrigin<Self::AccountId>>;

        /// Origin that can submit amendments
        type SubmissionOrigin: EnsureOrigin<Self::Origin>;

        /// Origin that can veto amendments
        type VetoOrigin: EnsureOrigin<Self::Origin>;

        /// How much blocks have to be produced before executing the amendment
        type Delay: Get<Self::BlockNumber>;

        /// Weight information for extrinsics in this pallet.
        type WeightInfo: WeightInfo;
    }

    #[pallet::pallet]
    #[pallet::generate_store(pub(super) trait Store)]
    pub struct Pallet<T>(PhantomData<T>);

    #[pallet::hooks]
    impl<T: Config> Hooks<BlockNumberFor<T>> for Pallet<T> {}

    #[pallet::call]
    impl<T: Config> Pallet<T> {
        /// Schedule `amendment` to be executed after the configured time, unless vetoed by `VetoOrigin`
        #[pallet::weight(
			(
				T::WeightInfo::propose(
					amendment.using_encoded(|x| x.len()) as u32,
				).saturating_add(amendment.get_dispatch_info().weight),
				DispatchClass::Operational,
			)
		)]
        pub fn propose(
            origin: OriginFor<T>,
            amendment: Box<T::Amendment>,
        ) -> DispatchResultWithPostInfo {
            T::SubmissionOrigin::try_origin(origin)
                .map(|_| ())
                .or_else(ensure_root)?;

            let nb_scheduled = <AmendmentsScheduled<T>>::get();
            let scheduler_id = (AMENDMENTS_ID, nb_scheduled).encode();
            let when = <system::Pallet<T>>::block_number() + T::Delay::get();

            if T::Scheduler::schedule_named(
                scheduler_id,
                At(when),
                None,
                // This number defines a priority of execution of the scheduled calls. We basically took the number
                // from parity's democracy pallet and substracted 1 to make sure we have priority over it if a chain
                // uses both modules.
                62,
                system::RawOrigin::Root.into(),
                *amendment,
            )
            .is_err()
            {
                return Err(Error::<T>::FailedToScheduleAmendment.into());
            }

            <AmendmentsScheduled<T>>::put(nb_scheduled + 1);

            Self::deposit_event(Event::AmendmentScheduled(nb_scheduled, when));
            Ok(().into())
        }

        /// Veto and cancel a scheduled amendment
        #[pallet::weight(T::WeightInfo::veto())]
        pub fn veto(origin: OriginFor<T>, amendment_id: u64) -> DispatchResultWithPostInfo {
            T::VetoOrigin::try_origin(origin)
                .map(|_| ())
                .or_else(ensure_root)?;

            let scheduler_id = (AMENDMENTS_ID, amendment_id).encode();
            if T::Scheduler::cancel_named(scheduler_id).is_err() {
                return Err(Error::<T>::FailedToCancelAmendment.into());
            }

            Self::deposit_event(Event::AmendmentVetoed(amendment_id));
            Ok(().into())
        }
    }

    #[pallet::event]
    #[pallet::generate_deposit(pub(super) fn deposit_event)]
    pub enum Event<T: Config> {
        /// A new amendment has been scheduled to be executed at the given block number
        AmendmentScheduled(u64, T::BlockNumber),
        /// An amendment has been vetoed and will never be triggered
        AmendmentVetoed(u64),
    }

    #[pallet::error]
    pub enum Error<T> {
        /// We failed to schedule the amendment
        FailedToScheduleAmendment,
        /// We failed to cancel the amendment
        FailedToCancelAmendment,
    }

    #[pallet::storage]
    #[pallet::getter(fn amendments_scheduled)]
    pub type AmendmentsScheduled<T: Config> = StorageValue<_, u64, ValueQuery>;
}
