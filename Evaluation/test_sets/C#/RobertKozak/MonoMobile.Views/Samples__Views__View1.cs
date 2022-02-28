using System;
using MonoMobile.Views;

namespace Samples
{
	public class View1: View
	{
		public string Person 
		{ 
			get;// { return Get(()=>Person, "Robert"); } 
			set;// { Set(()=>Person, value); }
		} 
		[Entry]
		public int Age 
		{
			get { return INPC.Get(()=>Age, 43); } 
			set { INPC.Set(()=>Age, value); }
		}
		
		[Button]
		public void ChangeAge()
		{
			Age = 44;
			Person = "Robert";
		}
		
		private IObservableObject INPC;
		public View1()
		{
			INPC = new ObservableObject();
			

		}
	}

	public class View2 : View
	{
		public string Company { get; set; }
		[List]
		public AddressView AddressView { get; set; }
		
		public View2()
		{
			Company = "Nowcom Corporation";
			AddressView = new AddressView() {Number = "4751", Street ="Wilshire Blvd", City ="LA", State="CA", Zip ="90010" };
		}
	}
}

