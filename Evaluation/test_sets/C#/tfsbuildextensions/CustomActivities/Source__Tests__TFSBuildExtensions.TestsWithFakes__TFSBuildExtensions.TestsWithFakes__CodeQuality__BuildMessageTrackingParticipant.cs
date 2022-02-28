using Microsoft.TeamFoundation.Build.Workflow.Tracking;
using System;
using System.Activities.Tracking;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TfsBuildExtensions.TestsWithFakes.CodeQuality
{
    /// <summary>
    /// BuildMessageTrackingParticipant that logs build messages during build workflow activities
    /// </summary>
    public class BuildMessageTrackingParticipant : TrackingParticipant
    {
        private StringBuilder buildMessage = new StringBuilder(4096);

        public override string ToString()
        {
            return this.buildMessage.ToString();
        }
        protected override void Track(TrackingRecord record, TimeSpan timeout)
        {
            var buildMessage = record as BuildInformationRecord<BuildMessage>;
            if (buildMessage != null && buildMessage.Value != null)
            {
                this.buildMessage.AppendLine(buildMessage.Value.Message);
            }

            var buildWarning = record as BuildInformationRecord<BuildWarning>;
            if (buildWarning != null && buildWarning.Value != null)
            {
                this.buildMessage.AppendLine(buildWarning.Value.Message);
            }

            var buildError = record as BuildInformationRecord<BuildError>;
            if (buildError != null && buildError.Value != null)
            {
                this.buildMessage.AppendLine(buildError.Value.Message);
            }
        }
    }
}
