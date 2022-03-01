using Pacman.Resources.Langs;
using System.Drawing;

namespace Pacman
{
    public class Pacman : VgcApis.BaseClasses.Plugin
    {
        Services.Settings settings;
        Views.WinForms.FormMain formMain = null;

        // form=null;

        #region properties
        public override string Name => Properties.Resources.Name;
        public override string Version => Properties.Resources.Version;
        public override string Description => I18N.Description;

        // png source https://www.flaticon.com/free-icon/pacman_1191124#term=pacman&page=1&position=31
        public override Image Icon => Properties.Resources.pacman_x32;

        #endregion

        #region protected overrides
        protected override void Popup()
        {
            VgcApis.Misc.UI.Invoke(() =>
            {
                if (formMain != null)
                {
                    formMain.Activate();
                    return;
                }

                formMain = Views.WinForms.FormMain.CreateForm(settings);
                formMain.FormClosed += (s, a) => formMain = null;
                formMain.Show();
            });
        }

        protected override void Start(VgcApis.Interfaces.Services.IApiService api)
        {
            settings = new Services.Settings();
            settings.Run(api);
        }

        protected override void Stop()
        {
            VgcApis.Misc.UI.CloseFormIgnoreError(formMain);
            settings?.Cleanup();
        }
        #endregion
    }
}
