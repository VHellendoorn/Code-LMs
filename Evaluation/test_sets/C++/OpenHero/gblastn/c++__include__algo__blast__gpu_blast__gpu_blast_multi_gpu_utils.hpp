#ifndef __GPU_BLAST_MULTI_GPU_UTILS_H__
#define __GPU_BLAST_MULTI_GPU_UTILS_H__
#include <map>
#include <vector>
#include <string>
// CUDA runtime
#include <cuda_runtime.h>
#include <helper_cuda.h>
#include <algo/blast/gpu_blast/thread_work_queue.hpp>


#ifdef _MSC_VER
#include <windows.h>
#else
#include <pthread.h>
#endif

using namespace std;


class GpuObject
{
public:
	GpuObject(){};
	~GpuObject(){};
	virtual void CreateData(){};
protected:
	
private:
};

class ThreadLock;

struct GpuData 
{
	GpuObject* m_global;
	GpuObject* m_local;
};
struct GpuHandle 
{
	cudaDeviceProp Prop;
	bool InUsed;
	GpuData Data;
};

typedef map<int, GpuHandle*> GpuHandleMapType;
typedef pair<int, GpuHandle*>	GpuHandleMapPairType;

typedef map<unsigned long, int> ThreadGPUMapType;
typedef pair<unsigned long, int> ThreadGPUPairType;

class GpuBlastMultiGPUsUtils
{
	GpuBlastMultiGPUsUtils();
	~GpuBlastMultiGPUsUtils();

	GpuBlastMultiGPUsUtils(const GpuBlastMultiGPUsUtils&);
	GpuBlastMultiGPUsUtils& operator =(GpuBlastMultiGPUsUtils);

public:

	static GpuBlastMultiGPUsUtils & instance()
	{
		static GpuBlastMultiGPUsUtils s;
		return s;
	}

	int InitGPUs(bool use_gpu, int gpu_id);
	void ReleaseGPUs();

	void ThreadFetchGPU(int & gpu_id);
	void ThreadReplaceGPU();

	GpuHandle* GetCurrentGPUHandle();

	bool b_useGpu;
protected:
	
private:
	int i_GPU_N;
	vector<int> q_gpu_ids;
	GpuHandleMapType mt_GPU;
	ThreadGPUMapType mt_threads;

	int select_id;

	ThreadLock mt_lock;

	int i_num_limited;
};

extern GpuBlastMultiGPUsUtils& BlastMGPUUtil;


#endif //__GPU_BLAST_MULTI_GPU_UTILS_H__