using XamCnblogs.Portable.Helpers;
using XamCnblogs.Portable.Model;
using XamCnblogs.Portable.Services;
using MvvmHelpers;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Windows.Input;
using Xamarin.Forms;
using Microsoft.AppCenter.Crashes;

namespace XamCnblogs.Portable.ViewModel
{
    public class KbArticlesViewModel : ViewModelBase
    {
        public ObservableRangeCollection<KbArticles> KbArticles { get; } = new ObservableRangeCollection<KbArticles>();
        public DateTime NextRefreshTime { get; set; }
        private int pageIndex = 1;
        private int pageSize = 20;
        public KbArticlesViewModel()
        {
            CanLoadMore = false;
        }
        public async void GetClientKbArticlesAsync()
        {
            KbArticles.AddRange(await SqliteUtil.Current.QueryKbArticles(pageSize));
        }
        ICommand refreshCommand;
        public ICommand RefreshCommand =>
            refreshCommand ?? (refreshCommand = new Command(async () =>
            {
                try
                {
                    IsBusy = true;
                    NextRefreshTime = DateTime.Now.AddMinutes(15);
                    CanLoadMore = false;
                    pageIndex = 1;
                    await ExecuteRefreshCommandAsync();
                }
                catch (Exception ex)
                {
                    Crashes.TrackError(ex);
                    LoadStatus = LoadMoreStatus.StausFail;
                }
                finally
                {
                    IsBusy = false;
                }
            }));


        LoadMoreStatus loadStatus;
        public LoadMoreStatus LoadStatus
        {
            get { return loadStatus; }
            set { SetProperty(ref loadStatus, value); }
        }
        ICommand loadMoreCommand;
        public ICommand LoadMoreCommand =>
            loadMoreCommand ?? (loadMoreCommand = new Command(async () =>
            {
                try
                {
                    LoadStatus = LoadMoreStatus.StausLoading;
                    await ExecuteRefreshCommandAsync();
                }
                catch (Exception)
                {
                    LoadStatus = LoadMoreStatus.StausError;
                }
            }));
        async Task ExecuteRefreshCommandAsync()
        {
            var result = await StoreManager.KbArticlesService.GetKbArticlesAsync(pageIndex, pageSize);
            if (result.Success)
            {
                var kbArticles = JsonConvert.DeserializeObject<List<KbArticles>>(result.Message.ToString());
                if (kbArticles.Count > 0)
                {
                    if (pageIndex == 1 && KbArticles.Count > 0)
                        KbArticles.Clear();
                    KbArticles.AddRange(kbArticles);
                    await SqliteUtil.Current.UpdateKbArticles(kbArticles);
                    pageIndex++;
                    LoadStatus = LoadMoreStatus.StausDefault;
                    CanLoadMore = true;
                }
                else
                {
                    CanLoadMore = false;
                    LoadStatus = pageIndex > 1 ? LoadMoreStatus.StausEnd : LoadMoreStatus.StausNodata;
                }
            }
            else
            {
                Crashes.TrackError(new Exception() { Source = result.Message });
                LoadStatus = pageIndex > 1 ? LoadMoreStatus.StausError : LoadMoreStatus.StausFail;
            }
        }

    }
}
