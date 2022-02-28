use crate::game::Game;
use crate::gui::utils::colors::DARK_BLUE;
use crate::gui::utils::draw_image;
use crate::gui::z::Z_UI_MENU;
use crate::net::NetMsg;
use crate::prelude::*;
use div::doc;
use paddle::FrameHandle;
use paddle::{quicksilver_compat::Shape, FitStrategy, Frame, NutsCheck, Rectangle, Transform};
use wasm_bindgen::closure::Closure;
use wasm_bindgen::JsCast;
use web_sys::{Element, Node};

pub(crate) struct LeaderboardFrame {
    pane: div::DivHandle,
    table: Node,
    players_by_karma: Vec<(String, i64)>,
    page_size: usize,
    current_page: usize,
    total_pages: usize,
    header: [Element; 3],
    footer: [Element; 3],
}

// Events triggerd in the browser
#[derive(Default)]
struct EvNextPage;
#[derive(Default)]
struct EvPrevPage;

impl LeaderboardFrame {
    pub fn new() -> PadlResult<Self> {
        let area = Self::area();
        let pane = div::new_styled(
            area.x() as i32,
            area.y() as i32,
            area.width() as u32,
            area.height() as u32,
            r#"<section class="leaderboard"></section>"#,
            &[""],
            &[("color", "white")],
        )?;
        pane.hide()?;

        // table will be filled with rows as player data arrives (not loaded until view is opened)
        let table_node = pane.first_inner_node()?;

        // First line in table is the header, which we can already be load
        let header_left = doc()?.create_element("div")?;
        header_left.set_text_content(Some("#"));
        table_node.append_child(&header_left)?;

        let header_middle = doc()?.create_element("div")?;
        header_middle.set_text_content(Some("Player")); // TODO: i18n
        table_node.append_child(&header_middle)?;

        let header_right = doc()?.create_element("div")?;
        header_right.set_text_content(Some("Karma")); // TODO: i18n, or maybe put the image instead?
        table_node.append_child(&header_right)?;

        // As the last row in table, there is a menu to select the displayed page.
        let menu_node = doc()?.create_element("div")?;
        pane.parent_element()?.append_child(&menu_node)?;

        let prev_button = doc()?.create_element("div")?;
        prev_button.set_text_content(Some(&"<"));
        share_on_click::<EvPrevPage>(&prev_button)?;

        let page_node = doc()?.create_element("div")?;
        page_node.set_text_content(Some(&"Page 1"));

        let next_button = doc()?.create_element("div")?;
        next_button.set_text_content(Some(&">"));
        share_on_click::<EvNextPage>(&next_button)?;

        Ok(LeaderboardFrame {
            pane,
            table: table_node,
            players_by_karma: vec![],
            page_size: 15, // TODO: find a fitting size dynamically
            current_page: 0,
            total_pages: 1,
            header: [header_left, header_middle, header_right],
            footer: [prev_button, page_node, next_button],
        })
    }
    pub fn init_listeners(frame_handle: FrameHandle<Self>) {
        frame_handle.listen(Self::network_message);
        frame_handle.listen(Self::next_page);
        frame_handle.listen(Self::prev_page);
    }
    pub fn clear(&self) {
        self.table.remove_all_children();
    }

    pub fn insert_row(&self, rank: usize, name: &str, karma: i64) -> PadlResult<()> {
        let node = doc()?.create_element("div")?;
        node.set_text_content(Some(&rank.to_string()));
        self.table.append_child(&node)?;

        let node = doc()?.create_element("div")?;
        node.set_text_content(Some(&name));
        self.table.append_child(&node)?;

        let node = doc()?.create_element("div")?;
        node.set_text_content(Some(&karma.to_string()));
        self.table.append_child(&node)?;

        Ok(())
    }

    pub fn network_message(&mut self, _state: &mut Game, msg: &NetMsg) {
        match msg {
            NetMsg::Leaderboard(offset, list, total_players) => {
                self.total_pages = (total_players + self.page_size - 1) / self.page_size;

                let required_len = offset + list.len();
                if self.players_by_karma.len() < required_len {
                    self.players_by_karma
                        .resize(required_len, Default::default())
                }
                for (i, (name, karma)) in list.into_iter().enumerate() {
                    self.players_by_karma[offset + i] = (name.clone(), *karma);
                }
                if self.current_page * self.page_size < required_len
                    && (self.current_page + 1) * self.page_size > *offset
                {
                    self.reload().nuts_check();
                }
            }
            _ => {}
        }
    }
    fn reload(&mut self) -> PadlResult<()> {
        self.clear();

        for element in &self.header {
            self.table.append_child(element)?;
        }

        let start = self.current_page * self.page_size;
        let end = (start + self.page_size).min(self.players_by_karma.len());
        for (i, (name, karma)) in self.players_by_karma[start..end].iter().enumerate() {
            self.insert_row(start + i + 1, &name, *karma)?;
        }
        self.footer[1].set_text_content(Some(&format!(
            "Page {} / {}",
            self.current_page + 1,
            self.total_pages
        )));

        for element in &self.footer {
            self.table.append_child(element)?;
        }

        Ok(())
    }
    fn is_loading(&self) -> bool {
        let start = self.current_page * self.page_size;
        self.players_by_karma
            .get(start)
            .map(|(name, _karma)| name.len() == 0)
            .unwrap_or(true)
    }
    fn next_page(&mut self, _state: &mut Game, _msg: &EvNextPage) {
        self.current_page = (self.current_page + 1).min(self.total_pages - 1);
        self.request_current_page();
    }
    fn prev_page(&mut self, _state: &mut Game, _msg: &EvPrevPage) {
        self.current_page = self.current_page.saturating_sub(1);
        self.request_current_page();
    }
    fn request_current_page(&mut self) {
        crate::net::request_leaderboard(self.current_page as i64, self.page_size as i64);
    }
}

impl Frame for LeaderboardFrame {
    type State = Game;
    const WIDTH: u32 = crate::resolution::MAIN_AREA_W;
    const HEIGHT: u32 = crate::resolution::MAIN_AREA_H;
    fn draw(&mut self, state: &mut Self::State, window: &mut paddle::DisplayArea, timestamp: f64) {
        window.fill(&DARK_BLUE);
        if self.is_loading() {
            let rot = timestamp / 2000.0 * 360.0;
            let splash_area = Rectangle::new_sized((500.0, 500.0)).with_center(Self::size() / 2.0);
            draw_image(
                &mut state.sprites,
                window,
                &splash_area,
                SpriteIndex::Simple(SingleSprite::Karma),
                Z_UI_MENU,
                FitStrategy::Center,
                Transform::rotate(rot),
            );
        }
    }
    fn enter(&mut self, _state: &mut Self::State) {
        self.request_current_page();
        self.pane.show().nuts_check();
    }
    fn leave(&mut self, _state: &mut Self::State) {
        self.pane.hide().nuts_check();
    }
}

fn share_on_click<A: std::any::Any + Default>(listener: &web_sys::Element) -> PadlResult<()> {
    let callback = || {
        paddle::share(A::default());
    };
    let closure = Closure::wrap(Box::new(callback) as Box<dyn FnMut()>);
    listener.add_event_listener_with_callback("click", closure.as_ref().dyn_ref().unwrap())?;
    closure.forget();
    Ok(())
}
