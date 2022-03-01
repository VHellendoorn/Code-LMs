// Author(s): Sébastien Lorion

using System;
using System.Globalization;
using System.Runtime.Serialization;

namespace NLight.IO.Text
{
	/// <summary>
	/// Represents the exception that is thrown when a duplicate column is found.
	/// </summary>
	[Serializable]
	public class DuplicateRecordColumnException
		: Exception
	{
		/// <summary>
		/// Contains the message that describes the error.
		/// </summary>
		private string _message;

		/// <summary>
		/// Initializes a new instance of the DuplicateColumnNameException class.
		/// </summary>
		public DuplicateRecordColumnException()
			: this(null, null)
		{
		}

		/// <summary>
		/// Initializes a new instance of the DuplicateColumnNameException class.
		/// </summary>
		/// <param name="columnName">The column name.</param>
		public DuplicateRecordColumnException(string columnName)
			: this(columnName, null)
		{
		}

		/// <summary>
		/// Initializes a new instance of the DuplicateColumnNameException class.
		/// </summary>
		/// <param name="columnName">The column name.</param>
		/// <param name="innerException">The exception that is the cause of the current exception.</param>
		public DuplicateRecordColumnException(string columnName, Exception innerException)
			: base(String.Empty, innerException)
		{
			this.ColumnName = columnName;

			_message = String.Format(CultureInfo.InvariantCulture, Resources.ExceptionMessages.IO_DuplicateColumnName, this.ColumnName);
		}

		/// <summary>
		/// Initializes a new instance of the DuplicateColumnNameException class.
		/// </summary>
		/// <param name="columnName">The column name.</param>
		/// <param name="message">The message that describes the error.</param>
		/// <param name="innerException">The exception that is the cause of the current exception.</param>
		public DuplicateRecordColumnException(string columnName, string message, Exception innerException)
			: base(String.Empty, innerException)
		{
			this.ColumnName = columnName;
			_message = (message == null ? string.Empty : message);
		}

		/// <summary>
		/// Initializes a new instance of the DuplicateColumnNameException class with serialized data.
		/// </summary>
		/// <param name="info">The <see cref="T:SerializationInfo"/> that holds the serialized object data about the exception being thrown.</param>
		/// <param name="context">The <see cref="T:StreamingContext"/> that contains contextual information about the source or destination.</param>
		protected DuplicateRecordColumnException(SerializationInfo info, StreamingContext context)
			: base(info, context)
		{
			_message = info.GetString("MyMessage");
			this.ColumnName = info.GetString("ColumnName");
		}

		/// <summary>
		/// Gets the column name.
		/// </summary>
		public string ColumnName { get; }

		#region Overrides

		/// <summary>
		/// Gets a message that describes the current exception.
		/// </summary>
		public override string Message => _message;

		/// <summary>
		/// When overridden in a derived class, sets the <see cref="T:SerializationInfo"/> with information about the exception.
		/// </summary>
		/// <param name="info">The <see cref="T:SerializationInfo"/> that holds the serialized object data about the exception being thrown.</param>
		/// <param name="context">The <see cref="T:StreamingContext"/> that contains contextual information about the source or destination.</param>
		public override void GetObjectData(System.Runtime.Serialization.SerializationInfo info, System.Runtime.Serialization.StreamingContext context)
		{
			base.GetObjectData(info, context);

			info.AddValue("MyMessage", _message);
			info.AddValue("ColumnName", this.ColumnName);
		}

		#endregion
	}
}