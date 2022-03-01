//-----------------------------------------------------------------------
// <copyright file="SearchMatch.cs">(c) http://TfsBuildExtensions.codeplex.com/. This source is subject to the Microsoft Permissive License. See http://www.microsoft.com/resources/sharedsource/licensingbasics/sharedsourcelicenses.mspx. All other rights reserved.</copyright>
// Contributed by Charlie Mott - http://geekswithblogs.net/charliemott/archive/2013/02/04/tfs-build-custom-activity--todo-counter.aspx
//-----------------------------------------------------------------------
namespace TfsBuildExtensions.Activities.TeamFoundationServer
{
    using System.IO;

    /// <summary>
    /// The search match.
    /// </summary>
    public class SearchMatch
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="SearchMatch"/> class.
        /// </summary>
        /// <param name="fileName"> The file name. </param>
        /// <param name="lineNumber"> The line number. </param>
        /// <param name="lineText"> The line text. </param>
        public SearchMatch(string fileName, int lineNumber, string lineText)
        {
            this.File = new FileInfo(fileName);
            this.LineNumber = lineNumber;
            this.LineText = lineText;
        }

        /// <summary>
        /// Gets or sets the file information.
        /// </summary>
        public FileInfo File { get; set; }

        /// <summary>
        /// Gets or sets the line number.
        /// </summary>
        public int LineNumber { get; set; }

        /// <summary>
        /// Gets or sets the line text.
        /// </summary>
        public string LineText { get; set; }
    }
}