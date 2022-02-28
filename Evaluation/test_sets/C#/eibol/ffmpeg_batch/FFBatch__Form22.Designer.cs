
namespace FFBatch
{
    partial class Form22
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(Form22));
            this.radio_filter = new System.Windows.Forms.RadioButton();
            this.radio_demuxer = new System.Windows.Forms.RadioButton();
            this.chk_copy = new System.Windows.Forms.CheckBox();
            this.chk_filter = new System.Windows.Forms.CheckBox();
            this.textBox1 = new System.Windows.Forms.TextBox();
            this.button1 = new System.Windows.Forms.Button();
            this.btn_cancel = new System.Windows.Forms.Button();
            this.panel1 = new System.Windows.Forms.Panel();
            this.txt_params = new System.Windows.Forms.TextBox();
            this.label1 = new System.Windows.Forms.Label();
            this.chk_batch_concat = new System.Windows.Forms.CheckBox();
            this.txt_intro = new System.Windows.Forms.TextBox();
            this.txt_end = new System.Windows.Forms.TextBox();
            this.txt_batch_concat = new System.Windows.Forms.TextBox();
            this.panel_batch = new System.Windows.Forms.Panel();
            this.btn_end = new System.Windows.Forms.Button();
            this.btn_intro = new System.Windows.Forms.Button();
            this.label3 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.txt_c_format = new System.Windows.Forms.TextBox();
            this.label4 = new System.Windows.Forms.Label();
            this.panel1.SuspendLayout();
            this.panel_batch.SuspendLayout();
            this.SuspendLayout();
            // 
            // radio_filter
            // 
            resources.ApplyResources(this.radio_filter, "radio_filter");
            this.radio_filter.Name = "radio_filter";
            this.radio_filter.TabStop = true;
            this.radio_filter.UseVisualStyleBackColor = true;
            this.radio_filter.CheckedChanged += new System.EventHandler(this.radio_filter_CheckedChanged);
            // 
            // radio_demuxer
            // 
            resources.ApplyResources(this.radio_demuxer, "radio_demuxer");
            this.radio_demuxer.Name = "radio_demuxer";
            this.radio_demuxer.TabStop = true;
            this.radio_demuxer.UseVisualStyleBackColor = true;
            this.radio_demuxer.CheckedChanged += new System.EventHandler(this.radio_demuxer_CheckedChanged);
            // 
            // chk_copy
            // 
            resources.ApplyResources(this.chk_copy, "chk_copy");
            this.chk_copy.Checked = true;
            this.chk_copy.CheckState = System.Windows.Forms.CheckState.Checked;
            this.chk_copy.Name = "chk_copy";
            this.chk_copy.UseVisualStyleBackColor = true;
            this.chk_copy.CheckedChanged += new System.EventHandler(this.chk_copy_CheckedChanged);
            // 
            // chk_filter
            // 
            resources.ApplyResources(this.chk_filter, "chk_filter");
            this.chk_filter.Checked = true;
            this.chk_filter.CheckState = System.Windows.Forms.CheckState.Checked;
            this.chk_filter.Name = "chk_filter";
            this.chk_filter.UseVisualStyleBackColor = true;
            this.chk_filter.CheckedChanged += new System.EventHandler(this.chk_filter_CheckedChanged);
            // 
            // textBox1
            // 
            resources.ApplyResources(this.textBox1, "textBox1");
            this.textBox1.BackColor = System.Drawing.Color.White;
            this.textBox1.Name = "textBox1";
            this.textBox1.ReadOnly = true;
            // 
            // button1
            // 
            resources.ApplyResources(this.button1, "button1");
            this.button1.Name = "button1";
            this.button1.UseVisualStyleBackColor = true;
            this.button1.Click += new System.EventHandler(this.button1_Click);
            // 
            // btn_cancel
            // 
            resources.ApplyResources(this.btn_cancel, "btn_cancel");
            this.btn_cancel.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.btn_cancel.Name = "btn_cancel";
            this.btn_cancel.UseVisualStyleBackColor = true;
            this.btn_cancel.Click += new System.EventHandler(this.btn_cancel_Click);
            // 
            // panel1
            // 
            resources.ApplyResources(this.panel1, "panel1");
            this.panel1.BackColor = System.Drawing.SystemColors.InactiveBorder;
            this.panel1.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.panel1.Controls.Add(this.btn_cancel);
            this.panel1.Controls.Add(this.button1);
            this.panel1.Name = "panel1";
            // 
            // txt_params
            // 
            resources.ApplyResources(this.txt_params, "txt_params");
            this.txt_params.Name = "txt_params";
            // 
            // label1
            // 
            resources.ApplyResources(this.label1, "label1");
            this.label1.Name = "label1";
            // 
            // chk_batch_concat
            // 
            resources.ApplyResources(this.chk_batch_concat, "chk_batch_concat");
            this.chk_batch_concat.Name = "chk_batch_concat";
            this.chk_batch_concat.UseVisualStyleBackColor = true;
            this.chk_batch_concat.CheckedChanged += new System.EventHandler(this.chk_batch_concat_CheckedChanged);
            // 
            // txt_intro
            // 
            resources.ApplyResources(this.txt_intro, "txt_intro");
            this.txt_intro.Name = "txt_intro";
            // 
            // txt_end
            // 
            resources.ApplyResources(this.txt_end, "txt_end");
            this.txt_end.Name = "txt_end";
            // 
            // txt_batch_concat
            // 
            resources.ApplyResources(this.txt_batch_concat, "txt_batch_concat");
            this.txt_batch_concat.BackColor = System.Drawing.Color.White;
            this.txt_batch_concat.Name = "txt_batch_concat";
            this.txt_batch_concat.ReadOnly = true;
            // 
            // panel_batch
            // 
            resources.ApplyResources(this.panel_batch, "panel_batch");
            this.panel_batch.Controls.Add(this.btn_end);
            this.panel_batch.Controls.Add(this.btn_intro);
            this.panel_batch.Controls.Add(this.label3);
            this.panel_batch.Controls.Add(this.label2);
            this.panel_batch.Controls.Add(this.txt_intro);
            this.panel_batch.Controls.Add(this.txt_batch_concat);
            this.panel_batch.Controls.Add(this.txt_end);
            this.panel_batch.Name = "panel_batch";
            // 
            // btn_end
            // 
            resources.ApplyResources(this.btn_end, "btn_end");
            this.btn_end.Name = "btn_end";
            this.btn_end.UseVisualStyleBackColor = true;
            this.btn_end.Click += new System.EventHandler(this.btn_end_Click);
            // 
            // btn_intro
            // 
            resources.ApplyResources(this.btn_intro, "btn_intro");
            this.btn_intro.Name = "btn_intro";
            this.btn_intro.UseVisualStyleBackColor = true;
            this.btn_intro.Click += new System.EventHandler(this.btn_intro_Click);
            // 
            // label3
            // 
            resources.ApplyResources(this.label3, "label3");
            this.label3.Name = "label3";
            // 
            // label2
            // 
            resources.ApplyResources(this.label2, "label2");
            this.label2.Name = "label2";
            // 
            // txt_c_format
            // 
            resources.ApplyResources(this.txt_c_format, "txt_c_format");
            this.txt_c_format.Name = "txt_c_format";
            // 
            // label4
            // 
            resources.ApplyResources(this.label4, "label4");
            this.label4.Name = "label4";
            // 
            // Form22
            // 
            resources.ApplyResources(this, "$this");
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.BackColor = System.Drawing.Color.White;
            this.Controls.Add(this.panel1);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.txt_c_format);
            this.Controls.Add(this.panel_batch);
            this.Controls.Add(this.chk_batch_concat);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.txt_params);
            this.Controls.Add(this.textBox1);
            this.Controls.Add(this.chk_filter);
            this.Controls.Add(this.chk_copy);
            this.Controls.Add(this.radio_demuxer);
            this.Controls.Add(this.radio_filter);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedSingle;
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Name = "Form22";
            this.Load += new System.EventHandler(this.Form22_Load);
            this.panel1.ResumeLayout(false);
            this.panel_batch.ResumeLayout(false);
            this.panel_batch.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion
        private System.Windows.Forms.CheckBox chk_copy;
        private System.Windows.Forms.CheckBox chk_filter;
        private System.Windows.Forms.TextBox textBox1;
        private System.Windows.Forms.Button button1;
        private System.Windows.Forms.Button btn_cancel;
        private System.Windows.Forms.Panel panel1;
        private System.Windows.Forms.Label label1;
        public System.Windows.Forms.TextBox txt_params;
        public System.Windows.Forms.RadioButton radio_filter;
        public System.Windows.Forms.RadioButton radio_demuxer;
        public System.Windows.Forms.TextBox txt_intro;
        public System.Windows.Forms.TextBox txt_end;
        private System.Windows.Forms.TextBox txt_batch_concat;
        private System.Windows.Forms.Panel panel_batch;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Button btn_end;
        private System.Windows.Forms.Button btn_intro;
        public System.Windows.Forms.CheckBox chk_batch_concat;
        public System.Windows.Forms.TextBox txt_c_format;
        private System.Windows.Forms.Label label4;
    }
}