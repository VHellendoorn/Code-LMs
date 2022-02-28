using Naxam.Controls.Forms;
using System;
using Xamarin.Forms;
using XamCnblogs.Portable.Helpers;
using XamCnblogs.Portable.Model;
using XamCnblogs.UI.Pages.Account;

namespace XamCnblogs.UI.Pages.Question {
    public class QuestionsTopTabbedPage : TopTabbedPage {
        bool hasInitialization;
        public QuestionsTopTabbedPage() {
            Title = "博问";
            Icon = "menu_question.png";

            BarTextColor = (Color)Application.Current.Resources["TitleText"];
            BarIndicatorColor = (Color)Application.Current.Resources["TitleText"];
            BarBackgroundColor = (Color)Application.Current.Resources["NavigationText"];

            if (Device.iOS == Device.RuntimePlatform) {
                var cancel = new ToolbarItem {
                    Text = "搜索",
                    Command = new Command(async () => {
                        await NavigationService.PushAsync(Navigation, new QuestionsSearchPage());
                    }),
                    Icon = "toolbar_search.png"
                };
                ToolbarItems.Add(cancel);
            }
        }
        protected override void OnAppearing() {
            base.OnAppearing();

            if (!hasInitialization) {
                this.Children.Add(new QuestionsPage() { Title = "待解决" });
                this.Children.Add(new QuestionsPage(1) { Title = "高分" });
                this.Children.Add(new QuestionsPage(2) { Title = "没有答案" });
                this.Children.Add(new QuestionsPage(3) { Title = "已解决" });
                this.Children.Add(new QuestionsPage(4) { Title = "我的问题" });

                hasInitialization = true;
            }
        }
    }
}
