
/////////////////////////////////////////////////////////////////////////////////////////
//
// box_rayIntersect_box.h
//
// Sergei <Neill3d> Solokhin 2014-2018
//
// GitHub page - https://github.com/Neill3d/OpenMoBu
// Licensed under The "New" BSD License - https ://github.com/Neill3d/OpenMoBu/blob/master/LICENSE
//
/////////////////////////////////////////////////////////////////////////////////////////

#ifndef __BOX_RAY_INTERSECT_BOX_H__
#define __BOX_RAY_INTERSECT_BOX_H__


//--- SDK include
#include <fbsdk/fbsdk.h>

//--- Registration defines
#define	BOXRAYINTERSECT__CLASSNAME		Box_RayIntersect
#define BOXRAYINTERSECT__CLASSSTR		"Box_RayIntersect"

#define	BOXSPHERECOORDS__CLASSNAME		Box_SphereCoords
#define BOXSPHERECOORDS__CLASSSTR		"Box_SphereCoords"

class RayIntersector
{
public:
	RayIntersector( FBVector3d pos, FBVector3d dir )
		: mPos(pos)
		, mDir(dir)
	{
		mGeometry = nullptr;
		mMesh = nullptr;
	}

	bool intersectModel( FBModel *pModel ); 

	struct IntersectionInfo
	{
		bool		finded;
		int			facet;	// intersection face
		FBVector3d	point;	// ray intersection point
		double		u;		// facet u coord
		double		v;		// facet v coord
	} info;

public:
	FBGeometry		*mGeometry;
	FBMesh			*mMesh;

	FBVector3d		mPos;
	FBVector3d		mDir;

	// calc intersection with a tri
	double calcIntersection( FBVector3d R0, FBVector3d R1, FBVector3d a, FBVector3d b, FBVector3d c );
	// p - point on plane
	bool checkInside( FBVertex a, FBVertex b, FBVertex c, FBVector3d p );

	int intersect_triangle( FBVector3d orig, FBVector3d dir, FBVector3d vert0, FBVector3d vert1, FBVector3d vert2,
		double &t, double &u, double &v );
};


/**	Box_RayIntersect
*/
class Box_RayIntersect : public FBBox
{
	//--- box declaration.
	FBBoxDeclare( Box_RayIntersect, FBBox );

public:
	//! creation function.
	virtual bool FBCreate();

	//! destruction function.
	virtual void FBDestroy();

	//! Overloaded FBBox real-time evaluation function.
	virtual bool AnimationNodeNotify(FBAnimationNode *pAnimationNode, FBEvaluateInfo *pEvaluateInfo);

	//! FBX Storage function
	virtual bool FbxStore( FBFbxObject *pFbxObject, kFbxObjectStore pStoreWhat );

	//! FBX Retrieval function
	virtual bool FbxRetrieve( FBFbxObject *pFbxObject, kFbxObjectStore pStoreWhat );

private:

	FBAnimationNode		*mNodeMesh;		//!> input - mesh node
	FBAnimationNode		*mRayStart;		//!> input - ray start position
	FBAnimationNode		*mRayDirection;	//!> input - ray direction point

	FBAnimationNode		*mIntersectPoint;	//!> output - mesh intersection world point
	FBAnimationNode		*mIntersectNormal;	//!> output - mesh intersection normal
	FBAnimationNode		*mUVCoords[2];		//!> output - intersection u,v points		
};

////////////////////////////////////////////////////////////////////////////////////////////////////////
// Box SphereCoords

class Box_SphereCoords : public FBBox
{
	//--- box declaration.
	FBBoxDeclare( Box_SphereCoords, FBBox );

public:
	//! creation function.
	virtual bool FBCreate();

	//! destruction function.
	virtual void FBDestroy();

	//! Overloaded FBBox real-time evaluation function.
	virtual bool AnimationNodeNotify(FBAnimationNode *pAnimationNode, FBEvaluateInfo *pEvaluateInfo);

	//! FBX Storage function
	virtual bool FbxStore( FBFbxObject *pFbxObject, kFbxObjectStore pStoreWhat );

	//! FBX Retrieval function
	virtual bool FbxRetrieve( FBFbxObject *pFbxObject, kFbxObjectStore pStoreWhat );

private:

	FBAnimationNode		*mNodeMesh;		//!> input - mesh node - use this mesh to untransform a ray and sphere coords
	FBAnimationNode		*mRayStart;		//!> input - ray start position
	FBAnimationNode		*mRayDirection;	//!> input - ray direction point

	FBAnimationNode		*mSphereCoords[2];		//!> output - inclination, azimuth
	FBAnimationNode		*mUVCoords[2];		//!> output - result u,v coords of a point
};


#endif /* __BOX_RAY_INTERSECT_BOX_H__ */
