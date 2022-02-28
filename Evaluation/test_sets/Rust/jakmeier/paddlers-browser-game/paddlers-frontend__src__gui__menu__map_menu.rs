use crate::game::Game;
use crate::gui::{
    gui_components::{ResourcesComponent, TableTextProvider},
    input::left_click::MapLeftClickSystem,
    input::MouseState,
    menu::*,
    ui_state::UiState,
};
use crate::prelude::*;
use paddle::Frame;
use specs::prelude::*;

pub(crate) struct MapMenuFrame<'a, 'b> {
    text_provider: TableTextProvider,
    left_click_dispatcher: Dispatcher<'a, 'b>,
    _hover_component: ResourcesComponent,
    mouse: PointerTracker,
}
impl MapMenuFrame<'_, '_> {
    pub fn new() -> PadlResult<Self> {
        let left_click_dispatcher = DispatcherBuilder::new()
            .with(MapLeftClickSystem::new(), "", &[])
            .build();

        Ok(MapMenuFrame {
            text_provider: TableTextProvider::new(),
            left_click_dispatcher,
            _hover_component: ResourcesComponent::new()?,
            mouse: PointerTracker::new(),
        })
    }
    fn left_click(&mut self, state: &mut Game, mouse_pos: Vector) {
        let ms = MouseState(mouse_pos);
        state.world.insert(ms);
        self.left_click_dispatcher.dispatch(&state.world);
    }
}
impl<'a, 'b> Frame for MapMenuFrame<'a, 'b> {
    type State = Game;
    const WIDTH: u32 = crate::gui::menu::INNER_MENU_AREA_W as u32;
    const HEIGHT: u32 = crate::gui::menu::INNER_MENU_AREA_H as u32;

    fn draw(&mut self, state: &mut Self::State, window: &mut DisplayArea, _timestamp: f64) {
        self.text_provider.reset();
        let inner_area = Self::area();

        let selected_entity = state.world.fetch::<UiState>().selected_entity;
        if let Some(e) = selected_entity {
            let (img_area, table_area) = menu_selected_entity_spacing(&inner_area);
            let world = &state.world;
            let sprites = &mut state.sprites;
            entity_details::draw_entity_img(world, sprites, window, e, &img_area);
            entity_details::draw_map_entity_details_table(
                world,
                sprites,
                window,
                e,
                &table_area,
                &mut self.text_provider,
                self.mouse.pos(),
            );
        }
        self.text_provider.finish_draw();
    }
    fn leave(&mut self, _state: &mut Self::State) {
        self.text_provider.hide();
    }
    fn pointer(&mut self, state: &mut Self::State, event: PointerEvent) {
        self.mouse.track_pointer_event(&event);
        if let PointerEvent(PointerEventType::PrimaryClick, pos) = event {
            self.left_click(state, pos)
        }
    }
}
