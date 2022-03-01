//-----------------------------------------------------------------------
// <copyright file="RequestSpotInstances.cs">(c) http://TfsBuildExtensions.codeplex.com/. This source is subject to the Microsoft Permissive License. See http://www.microsoft.com/resources/sharedsource/licensingbasics/sharedsourcelicenses.mspx. All other rights reserved.</copyright>
//-----------------------------------------------------------------------
namespace TfsBuildExtensions.Activities.AWS.EC2
{
    using System;
    using System.Activities;
    using System.Collections.Generic;
    using System.ServiceModel;
    using Amazon.EC2.Model;
    using Microsoft.TeamFoundation.Build.Client;

    /// <summary>
    /// Create an instance on the EC2 spot market.
    /// </summary>
    [BuildActivity(HostEnvironmentOption.All)]
    public class RequestSpotInstances : BaseAmazonActivity
    {
        /// <summary>
        /// Gets or sets the number of instances to request.
        /// </summary>
        [RequiredArgument]
        public InArgument<int> InstanceCount { get; set; }

        /// <summary>
        /// Gets or sets the maximum price you are willing to pay.
        /// </summary>
        [RequiredArgument]
        public InArgument<decimal> SpotPrice { get; set; }

        /// <summary>
        /// Gets or sets the type of spot request.
        /// </summary>
        [RequiredArgument]
        public InArgument<string> RequestType { get; set; }

        /// <summary>
        /// Gets or sets the name of the AMI image.
        /// </summary>
        [RequiredArgument]
        public InArgument<string> ImageId { get; set; }

        /// <summary>
        /// Gets or sets the type of instance desired.
        /// </summary>
        [RequiredArgument]
        public InArgument<string> InstanceType { get; set; }

        /// <summary>
        /// Gets or sets the name of the security group to associate the launch with.
        /// </summary>
        public InArgument<string> SecurityGroupName { get; set; }

        /// <summary>
        /// Gets or sets the duration of the spot request starting at the current time.
        /// </summary>
        public InArgument<int> ValidDurationMinutes { get; set; }

        /// <summary>
        /// Gets or sets the list of instance reservations.
        /// </summary>
        public OutArgument<List<SpotInstanceRequest>> SpotRequests { get; set; }

        /// <summary>
        /// Create a new instance using the Amazon spot market.
        /// </summary>
        protected override void AmazonExecute()
        {
            try
            {
                // Set the Amazon instance request times
                //// var localValidFrom = DateTime.Now.ToAmazonDateTime();
                //// var localValidUntil = DateTime.Now.AddMinutes(this.ValidDurationMinutes.Get(this.ActivityContext)).ToAmazonDateTime();

                var request = new RequestSpotInstancesRequest
                {
                    //// AvailabilityZoneGroup = ,
                    InstanceCount = this.InstanceCount.Get(this.ActivityContext),
                    //// LaunchGroup = ,
                    LaunchSpecification = new LaunchSpecification()
                    {
                        ImageId = this.ImageId.Get(this.ActivityContext),
                        InstanceType = this.InstanceType.Get(this.ActivityContext),
                        SecurityGroup = new List<string>() { this.SecurityGroupName.Get(this.ActivityContext) }
                    },
                    SpotPrice = this.SpotPrice.Get(this.ActivityContext).ToString(),
                    Type = this.RequestType.Get(this.ActivityContext),
                    //// ValidFrom = localValidFrom,
                    //// ValidUntil = localValidUntil
                };

                try
                {
                    var response = EC2Client.RequestSpotInstances(request);
                    this.SpotRequests.Set(this.ActivityContext, response.RequestSpotInstancesResult.SpotInstanceRequest);
                }
                catch (EndpointNotFoundException ex)
                {
                    this.LogBuildMessage(ex.Message);
                }
            }
            catch (Exception ex)
            {
                this.LogBuildError("Amazon error: " + ex.Message + " Stack Trace: " + ex.StackTrace);
                if (ex.InnerException != null)
                {
                    this.LogBuildError("Inner exception: " + ex.InnerException.Message);
                }
            }
        }
    }
}