/*
 * Copyright 2021 Julian Schmidhuber <github@schmiddi.anonaddy.com>
 *
 * This file is part of Tubefeeder.
 *
 * Tubefeeder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tubefeeder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tubefeeder.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

use relm::StreamHandle;

/// A builder for the list elements of `LazyList`.
pub trait ListElementBuilder<W: relm::Widget> {
    /// Return the next batch of list elements to insert into the list.
    fn poll(&mut self) -> Vec<W::ModelParam>;

    /// Used for sending messages to the created widgets.
    fn add_stream(&mut self, _stream: StreamHandle<W::Msg>) {}

    /// Get the signal to emit to the clicked row.
    /// None if no signal should be sent.
    fn get_clicked_signal(&self) -> Option<W::Msg> {
        None
    }
}
