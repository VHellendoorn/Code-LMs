//-----------------------------------------------------------------------
// <copyright file="XmlTests.cs">(c) http://TfsBuildExtensions.codeplex.com/. This source is subject to the Microsoft Permissive License. See http://www.microsoft.com/resources/sharedsource/licensingbasics/sharedsourcelicenses.mspx. All other rights reserved.</copyright>
//-----------------------------------------------------------------------
namespace TfsBuildExtensions.Activities.Tests.Xml
{
    using System;
    using System.Activities;
    using System.Collections.Generic;
    using System.IO;
    using Microsoft.VisualStudio.TestTools.UnitTesting;
    using TfsBuildExtensions.Activities.Xml;

    [TestClass]
    public class XmlTests
    {
        [TestMethod]
        public void XmlFile_ValidateXmlTest()
        {
            // Arrange
            var target = new TfsBuildExtensions.Activities.Xml.Xml { Action = XmlAction.Transform };

            // Define activity arguments
            var arguments = new Dictionary<string, object>
            {
                { "XmlText", @"<?xml version=""1.0""?>
                    <catalog>
                       <book id=""bk101"">
                          <author>Gambardella, Matthew</author>
                          <title>XML Developer's Guide</title>
                          <genre>Computer</genre>
                          <price>44.95</price>
                          <publish_date>2000-10-01</publish_date>
                          <description>An in-depth look at creating applications 
                          with XML.</description>
                       </book>
                    </catalog>
                    " },
                { "XslTransform", @"<xsl:transform version=""1.0"" xmlns:xsl=""http://www.w3.org/1999/XSL/Transform""/>" },
            };

            // Act
            WorkflowInvoker invoker = new WorkflowInvoker(target);
            var result = invoker.Invoke(arguments);

            // Assert
            Assert.IsFalse((bool)result["IsValid"]);
        }
    }
}
