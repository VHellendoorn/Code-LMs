using System;

namespace Wintellect.Sterling.Core.Events
{
    /// <summary>
    ///     Notify arguments when changes happen
    /// </summary>
    public class SterlingOperationArgs : EventArgs 
    {
        public SterlingOperationArgs(SterlingOperation operation, Type targetType, object key)
        {
            TargetType = targetType;
            Operation = operation;
            Key = key;
        }          
  
        public Type TargetType { get; private set; }

        public object Key { get; private set; }

        public SterlingOperation Operation { get; private set; }
    }
}
