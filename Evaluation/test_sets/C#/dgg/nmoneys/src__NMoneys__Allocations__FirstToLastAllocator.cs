namespace NMoneys.Allocations
{
	internal class FirstToLastAllocator : RemainderAllocatorBase
	{
		public override Allocation Allocate(Allocation allocatedSoFar)
		{
			int index = 0;
			Allocation beingAllocated = allocatedSoFar;
			while (!beingAllocated.IsComplete && index < beingAllocated.Length)
			{
				beingAllocated = apply(beingAllocated, index);
				index++;
			}
			return beingAllocated;
		}
	}
}