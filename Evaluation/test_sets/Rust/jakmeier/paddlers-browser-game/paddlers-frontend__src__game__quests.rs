use super::{
    player_info::PlayerState, toplevel::Signal, town::Town, town_resources::TownResources, Game,
};
use crate::{
    net::{graphql::PlayerQuest, NetMsg},
    prelude::TextDb,
    resolution::{MAIN_AREA_H, MAIN_AREA_W},
};
use mogwai::prelude::*;
use paddle::{Frame, FrameHandle};

/// A Mogwai component ot display a single quest
mod quest_component;
/// Conditions to meet to finish a quest.
mod quest_conditions;
/// A Mogwai component ot display a list of quests
mod quest_list;
/// Effects and rewards receives when finishing a quest.
mod quest_rewards;

use quest_component::*;
use quest_list::*;

pub(crate) struct QuestsFrame {
    /// For communication with spawned view
    quests_gizmo: Gizmo<QuestList>,
    /// For keeping component alive
    quests_view: View<HtmlElement>,
}

#[derive(Clone, Debug)]

struct QuestUiTexts {
    title: String,
    rewards: String,
    conditions: String,
}

impl QuestUiTexts {
    fn new(locale: &TextDb) -> Self {
        Self {
            title: locale.gettext("quests").to_owned(),
            rewards: locale.gettext("reward").to_owned(),
            conditions: locale.gettext("your-task").to_owned(),
        }
    }
}

struct NewParent(HtmlElement);

impl Frame for QuestsFrame {
    type State = Game;
    const WIDTH: u32 = MAIN_AREA_W;
    const HEIGHT: u32 = MAIN_AREA_H;
}
impl QuestsFrame {
    pub fn new() -> Self {
        let quest_list = QuestList::new();
        let quests_gizmo = Gizmo::from(quest_list);
        let quests_view = View::from(quests_gizmo.view_builder());

        Self {
            quests_view,
            quests_gizmo,
        }
    }
    pub fn init_listeners(frame_handle: FrameHandle<Self>) {
        frame_handle.listen(Self::network_message);
        frame_handle.listen(Self::attach_to_parent);
        frame_handle.listen(Self::signal);
        let div = frame_handle.div();
        paddle::share(NewParent(div.parent_element().unwrap()))
    }

    fn network_message(&mut self, state: &mut Game, msg: &NetMsg) {
        match msg {
            NetMsg::Quests(data) => {
                self.reset_quests();
                for quest in data {
                    self.add_quest(
                        quest,
                        &state.locale,
                        &state.town(),
                        &state.town_world().fetch::<TownResources>(),
                        &state.player(),
                    );
                }
            }
            _ => {}
        }
    }
    fn signal(&mut self, state: &mut Game, msg: &Signal) {
        match msg {
            Signal::LocaleUpdated => {
                let locale = &state.locale;
                let ui_texts = QuestUiTexts::new(locale);
                self.quests_gizmo.send(&QuestListIn::NewLocale(ui_texts));
            }
            Signal::ResourcesUpdated => {
                let res = state
                    .town_world()
                    .fetch::<TownResources>()
                    .non_zero_resources();
                self.quests_gizmo.send(&QuestListIn::ResourceUpdate(res));
            }
            Signal::BuildingBuilt(b) => {
                self.quests_gizmo.send(&QuestListIn::BuildingChange(*b, 1));
            }
            Signal::BuildingRemoved(b) => {
                self.quests_gizmo.send(&QuestListIn::BuildingChange(*b, -1));
            }
            Signal::PlayerStateUpdated => self
                .quests_gizmo
                .send(&QuestListIn::PlayerState((*state.player()).clone())),
            Signal::NewWorker(t) => {
                self.quests_gizmo.send(&QuestListIn::WorkerChange(*t, 1));
            }
            Signal::WorkerStopped(t) => {
                self.quests_gizmo.send(&QuestListIn::WorkerChange(*t, -1));
            }
            _ => {}
        }
    }
    fn attach_to_parent(&mut self, _state: &mut Game, node: &NewParent) {
        node.0.append_child(&self.quests_view.dom_ref()).unwrap();
    }
    fn reset_quests(&mut self) {
        self.quests_gizmo.send(&QuestListIn::Clear);
    }
    fn add_quest(
        &mut self,
        quest: &PlayerQuest,
        locale: &TextDb,
        town: &Town,
        bank: &TownResources,
        player_info: &PlayerState,
    ) {
        self.quests_gizmo
            .send(&QuestListIn::NewQuestComponent(QuestComponent::new(
                quest,
                locale,
                town,
                bank,
                player_info,
            )))
    }
}
