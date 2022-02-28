using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using System.Drawing;
using System.Globalization;
using System.Linq;
using System.Runtime.Remoting.Messaging;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace FFBatch
{
    public partial class Form8 : Form
    {
        public String format_ext = String.Empty;
        public String url_dg_item = String.Empty;
        public String thumb_url_streams = String.Empty;
        public String format_ID = "";
        private Boolean working = true;

        public Form8()
        {
            InitializeComponent();
        }

        private void init_dg()
        {
            dg_streams.Columns[0].HeaderCell.Style.Alignment = DataGridViewContentAlignment.MiddleCenter;
            dg_streams.Columns[1].HeaderCell.Style.Alignment = DataGridViewContentAlignment.MiddleCenter;

            dg_streams.RowHeadersVisible = false;
            dg_streams.AutoSizeRowsMode = DataGridViewAutoSizeRowsMode.AllCellsExceptHeaders;
            dg_streams.RowsDefaultCellStyle.WrapMode = DataGridViewTriState.True;
            dg_streams.Columns[2].ReadOnly = true;
            dg_streams.Columns[3].ReadOnly = true;
            dg_streams.Columns[4].ReadOnly = true;
            dg_streams.Rows.Clear();
        }

        private void refresh_lang()
        {
            //Thread.CurrentThread.CurrentCulture = new CultureInfo(FFBatch.Properties.Settings.Default.app_lang, true);
            Thread.CurrentThread.CurrentUICulture = new CultureInfo(FFBatch.Properties.Settings.Default.app_lang, true);
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(Form8));
            RefreshResources(this, resources);
        }

        private void RefreshResources(Control ctrl, ComponentResourceManager res)
        {
            ctrl.SuspendLayout();
            this.InvokeEx(f => res.ApplyResources(ctrl, ctrl.Name, Thread.CurrentThread.CurrentUICulture));
            foreach (Control control in ctrl.Controls)
                RefreshResources(control, res); // recursion
            ctrl.ResumeLayout(false);
        }

        public void UpdateColorDark(Control myControl)
        {
            myControl.BackColor = Color.FromArgb(255, 64, 64, 64);
            myControl.ForeColor = Color.White;
            foreach (Control subC in myControl.Controls)
            {
                UpdateColorDark(subC);
            }
        }

        public void UpdateColorDefault(Control myControl)
        {
            myControl.BackColor = SystemColors.InactiveBorder;
            myControl.ForeColor = Control.DefaultForeColor;
            foreach (Control subC in myControl.Controls)
            {
                UpdateColorDefault(subC);
            }
        }

        private void Form8_Load(object sender, EventArgs e)
        {
            init_dg();
            if (Properties.Settings.Default.dark_mode == true)
            {
                foreach (Control c in this.Controls) UpdateColorDark(c);
                this.BackColor = Color.FromArgb(255, 64, 64, 64);
                dg_streams.BackgroundColor = Color.Gray;
                dg_streams.RowsDefaultCellStyle.BackColor = Color.Gray;
            }
            else
            {
                foreach (Control c in this.Controls) UpdateColorDefault(c);
                this.BackColor = SystemColors.InactiveBorder;
                dg_streams.BackgroundColor = SystemColors.InactiveBorder;
                dg_streams.RowsDefaultCellStyle.BackColor = Color.White;
            }
            btn_close.Focus();

            Process yt = new Process();
            Task t2 = Task.Run(() =>
            {
                yt.StartInfo.FileName = System.IO.Path.Combine(Application.StartupPath, "yt-dlp.exe");
                yt.StartInfo.Arguments = "-F " + url_dg_item;
                yt.StartInfo.RedirectStandardOutput = true;
                yt.StartInfo.UseShellExecute = false;
                yt.StartInfo.CreateNoWindow = true;
                yt.EnableRaisingEvents = true;
                yt.Start();
                String result = String.Empty;
                String stream = "";
                while (!yt.StandardOutput.EndOfStream)
                {
                    stream = yt.StandardOutput.ReadLine();
                    //MessageBox.Show(stream);
                    result = result + stream;
                    if (stream != null && stream != String.Empty && !stream.ToLower().Contains("format code") && !stream.ToLower().Contains("downloading webpage") && !stream.ToLower().Contains("available formats") && !stream.ToLower().Contains("downloading mpd manifest") && !stream.ToLower().Contains("downloading m3u8 information") && !stream.ToLower().Contains("downloading android player api json") && !stream.ToLower().Contains("available formats") && !stream.ToLower().Contains("downloading mpd manifest") && !stream.ToLower().Contains("downloading m3u8 information") && !stream.ToLower().Contains("id  ext  resolution fps") && !stream.ToLower().Contains("--------"))
                    {
                        try
                        {
                            dg_streams.Invoke(new MethodInvoker(delegate
                            {
                                String[] split = stream.Split(' ');
                                String Bitrate = "";
                                foreach (String str in split)
                                {
                                    if (str.Length > 0)
                                    {
                                        if (str.Substring(str.Length - 1, 1).ToLower() == "k")
                                        {
                                            Bitrate = str;
                                            stream = stream.Replace(str, "");
                                            break;
                                        }
                                    }
                                }

                                int start_c = stream.LastIndexOf("|") + 1;
                                int str_l = stream.Length;
                                int length = str_l - start_c;
                                String codec = Regex.Replace(stream.Substring(start_c, length), " {2,}", " ").Replace("mp4_dash", "").Replace("webm_dash", "").Replace("m4a_dash", "").TrimEnd();
                                if (codec.Substring(codec.Length - 1, 1) == ",")
                                {
                                    codec = codec.Substring(0, codec.Length - 1);
                                }

                                String[] split2 = codec.Split(' ');
                                String resol = "";
                                foreach (String str in split2)
                                {
                                    if (str.Length > 0)
                                    {
                                        if (str.Substring(str.Length - 1, 1).ToLower() == "p")
                                        {
                                            resol = str;
                                            codec = codec.Replace(str, "");
                                            break;
                                        }
                                    }
                                }

                                if (stream.ToLower().Contains("audio only"))
                                {
                                    dg_streams.Rows.Add(image_streams.Images[1], false, stream.Substring(0, 3).TrimEnd(), stream.Substring(4, 4).TrimEnd(), "Audio", codec, "-", Bitrate);
                                }
                                else
                                {
                                    dg_streams.Rows.Add(image_streams.Images[0], false, stream.Substring(0, 3).TrimEnd(), stream.Substring(4, 4).TrimEnd(), stream.Substring(9, 9).TrimEnd(), codec, resol, Bitrate);
                                }
                            }));
                        }
                        catch
                        {
                            dg_streams.Rows.Add(image_streams.Images[1], false, Properties.Strings.error, "-", "-");
                        }
                    }
                }

                yt.WaitForExit();
                working = false;
                pic_wait_1.Invoke(new MethodInvoker(delegate
                { pic_wait_1.Visible = false; }));

                pic_yout.Invoke(new MethodInvoker(delegate
                { pic_yout.Visible = true; }));

                dg_streams.Invoke(new MethodInvoker(delegate
                { dg_streams.Visible = true; }));

                txt_file.Invoke(new MethodInvoker(delegate
                {
                    txt_file.Text = url_dg_item;
                    txt_name.Enabled = true;
                }));
                txt_name.Invoke(new MethodInvoker(delegate
                {
                    txt_name.Text = FFBatch.Properties.Strings.yt_str_av;
                }));

                yt.StartInfo.Arguments = String.Empty;
                if (result == String.Empty || result == null)
                {
                    this.Invoke(new MethodInvoker(delegate
                    {
                        btn_select.Enabled = false;
                        label1.Visible = true;
                    }));
                }
                else
                {
                    this.Invoke(new MethodInvoker(delegate
                    {
                        btn_close.Enabled = true;
                    }));
                }
                dg_streams.Invoke(new MethodInvoker(delegate
                {
                    dg_streams.Refresh();
                }));
            });
            dg_streams.Sort(dg_streams.Columns[4], ListSortDirection.Descending);

            this.Text = FFBatch.Properties.Strings.yt_str_av2;
            dg_streams.Columns[1].HeaderText = FFBatch.Properties.Strings.Use;
            dg_streams.Columns[2].HeaderText = FFBatch.Properties.Strings.format_id;
            dg_streams.Columns[3].HeaderText = FFBatch.Properties.Strings.extension;
            dg_streams.Columns[4].HeaderText = FFBatch.Properties.Strings.size;
            dg_streams.Columns[5].HeaderText = FFBatch.Properties.Strings.codec;
            dg_streams.Columns[6].HeaderText = FFBatch.Properties.Strings.resolution;
            dg_streams.Columns[7].HeaderText = FFBatch.Properties.Strings2.bitrate;
        }

        private void btn_close_Click(object sender, EventArgs e)
        {
            this.Close();
        }

        private void btn_select_Click(object sender, EventArgs e)
        {
            String f_vid = String.Empty;
            String f_aud = String.Empty;
            int i = 0;
            Boolean mark = false;

            foreach (DataGridViewRow row1 in dg_streams.Rows)
            {
                if (row1.Cells[1].Value.ToString().ToLower() == "true")
                {
                    mark = true;
                    break;
                }
            }
            if (mark == false)
            {
                MessageBox.Show(FFBatch.Properties.Strings.no_chk_str);
                return;
            }

            foreach (DataGridViewRow row in dg_streams.Rows)
            {
                if (Convert.ToBoolean(row.Cells[1].Value) == true)
                {
                    i++;
                    if (row.Cells[4].Value.ToString().ToLower().Contains("audio"))
                    {
                        f_aud = row.Cells[2].Value.ToString();
                    }
                    else
                    {
                        f_vid = row.Cells[2].Value.ToString();
                        format_ext = row.Cells[3].Value.ToString();
                    }
                    if (i > 2)
                    {
                        MessageBox.Show(FFBatch.Properties.Strings.two_comp_f);
                        return;
                    }
                }
            }

            if (f_vid == String.Empty) format_ID = f_aud;
            if (f_aud == String.Empty) format_ID = f_vid;
            if (f_vid != String.Empty && f_aud != String.Empty) format_ID = f_vid + "+" + f_aud;
            this.Close();
        }

        public string UppercaseFirst(string text)
        {
            // Check for empty string.
            if (string.IsNullOrEmpty(text))
            {
                return string.Empty;
            }

            // Return char and concat substring.
            return char.ToUpper(text[0]) + text.Substring(1);
        }

        private void Form8_FormClosing(object sender, FormClosingEventArgs e)
        {
            if (working == true) e.Cancel = true;
        }
    }
}