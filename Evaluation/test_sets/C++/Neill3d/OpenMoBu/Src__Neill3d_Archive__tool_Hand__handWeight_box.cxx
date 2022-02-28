
/////////////////////////////////////////////////////////////////////////////////////////
//
// Licensed under the "New" BSD License. 
//		License page - https://github.com/Neill3d/MoBu/blob/master/LICENSE
//
// GitHub repository - https://github.com/Neill3d/MoBu
//
// Author Sergey Solohin (Neill3d) 2014
//  e-mail to: s@neill3d.com
//		www.neill3d.com
/////////////////////////////////////////////////////////////////////////////////////////

/**	\file	handWeight_box.cxx
*/

//--- Class declaration
#include "handWeight_box.h"
#include <math.h>

//--- Registration defines
#define HANDWEIGHT1__CLASS			HANDWEIGHT1__CLASSNAME
#define HANDWEIGHT1__NAME			HANDWEIGHT1__CLASSSTR
#define	HANDWEIGHT1__LOCATION		"HandTool"
#define HANDWEIGHT1__LABEL			"Weight1"
#define	HANDWEIGHT1__DESC			"calculate weight for the first joint"

#define HANDWEIGHT2__CLASS			HANDWEIGHT2__CLASSNAME
#define HANDWEIGHT2__NAME			HANDWEIGHT2__CLASSSTR
#define	HANDWEIGHT2__LOCATION		"HandTool"
#define HANDWEIGHT2__LABEL			"Weight2"
#define	HANDWEIGHT2__DESC			"calculate weight for the second joint"

#define HANDWEIGHT3__CLASS			HANDWEIGHT3__CLASSNAME
#define HANDWEIGHT3__NAME			HANDWEIGHT3__CLASSSTR
#define	HANDWEIGHT3__LOCATION		"HandTool"
#define HANDWEIGHT3__LABEL			"Weight3"
#define	HANDWEIGHT3__DESC			"calculate weight for the third joint"

//--- implementation and registration
FBBoxImplementation	(	HANDWEIGHT1__CLASS		);	// Box class name
FBRegisterBox		(	HANDWEIGHT1__NAME,			// Unique name to register box.
						HANDWEIGHT1__CLASS,			// Box class name
						HANDWEIGHT1__LOCATION,		// Box location ('plugins')
						HANDWEIGHT1__LABEL,			// Box label (name of box to display)
						HANDWEIGHT1__DESC,			// Box long description.
						FB_DEFAULT_SDK_ICON			);	// Icon filename (default=Open Reality icon)

FBBoxImplementation	(	HANDWEIGHT2__CLASS		);	// Box class name
FBRegisterBox		(	HANDWEIGHT2__NAME,			// Unique name to register box.
						HANDWEIGHT2__CLASS,			// Box class name
						HANDWEIGHT2__LOCATION,		// Box location ('plugins')
						HANDWEIGHT2__LABEL,			// Box label (name of box to display)
						HANDWEIGHT2__DESC,			// Box long description.
						FB_DEFAULT_SDK_ICON			);	// Icon filename (default=Open Reality icon)

FBBoxImplementation	(	HANDWEIGHT3__CLASS		);	// Box class name
FBRegisterBox		(	HANDWEIGHT3__NAME,			// Unique name to register box.
						HANDWEIGHT3__CLASS,			// Box class name
						HANDWEIGHT3__LOCATION,		// Box location ('plugins')
						HANDWEIGHT3__LABEL,			// Box label (name of box to display)
						HANDWEIGHT3__DESC,			// Box long description.
						FB_DEFAULT_SDK_ICON			);	// Icon filename (default=Open Reality icon)

/************************************************
 *	Creation
 ************************************************/
bool CHandWeight1::FBCreate()
{
	if( FBBox::FBCreate() )
	{
		// Input Nodes
		mWeight	= AnimationNodeInCreate	( 0, "Weight", ANIMATIONNODE_TYPE_NUMBER );
		mInherit= AnimationNodeInCreate	( 0, "Inherit", ANIMATIONNODE_TYPE_VECTOR );
	
		// Output Node
		mResult		= AnimationNodeOutCreate( 3, "Result",	ANIMATIONNODE_TYPE_NUMBER );

		return true;
	}
	return false;
}


/************************************************
 *	Destruction.
 ************************************************/
void CHandWeight1::FBDestroy()
{
	FBBox::FBDestroy();
}


/************************************************
 *	Real-time engine evaluation
 ************************************************/
bool CHandWeight1::AnimationNodeNotify( FBAnimationNode *pAnimationNode, FBEvaluateInfo *pEvaluateInfo )
{
	double		lW, lR;
	FBVector3d	lV;
	bool			lStatus[2];

	// Read connector in values
	lStatus[0] = mWeight	->ReadData( &lW, pEvaluateInfo );
	lStatus[1] = mInherit	->ReadData( lV, pEvaluateInfo );

	// Set default values if no input connection.
	if( !lStatus[0] )
	{
		lW = 0.0;
	}
	if( !lStatus[1] )
	{
		lV[0] = 1.0;
	}

	// Calculate result.
	lR = (lW > 0.0) ? 100.0 - fabs(lW) : 100.0;
	
	lR	*= lV[0]*0.01;
	lR	*= 0.01;

	// Write result out to connector.
	mResult->WriteData( &lR, pEvaluateInfo );
	return true;
}


/************************************************
 *	FBX Storage.
 ************************************************/
bool CHandWeight1::FbxStore( FBFbxObject *pFbxObject, kFbxObjectStore pStoreWhat )
{
	/*
	*	Store box parameters.
	*/
	return true;
}


/************************************************
 *	FBX Retrieval.
 ************************************************/
bool CHandWeight1::FbxRetrieve(FBFbxObject *pFbxObject, kFbxObjectStore pStoreWhat )
{
	/*
	*	Retrieve box parameters.
	*/
	return true;
}


//--------------------------------------------------------------------------------------- WEIGHT 2

/************************************************
 *	Creation
 ************************************************/
bool CHandWeight2::FBCreate()
{
	if( FBBox::FBCreate() )
	{
		// Input Nodes
		mWeight	= AnimationNodeInCreate	( 0, "Weight", ANIMATIONNODE_TYPE_NUMBER );
		mInherit= AnimationNodeInCreate	( 0, "Inherit", ANIMATIONNODE_TYPE_VECTOR );
	
		// Output Node
		mResult		= AnimationNodeOutCreate( 3, "Result",	ANIMATIONNODE_TYPE_NUMBER );

		return true;
	}
	return false;
}


/************************************************
 *	Destruction.
 ************************************************/
void CHandWeight2::FBDestroy()
{
	FBBox::FBDestroy();
}


/************************************************
 *	Real-time engine evaluation
 ************************************************/
bool CHandWeight2::AnimationNodeNotify( FBAnimationNode *pAnimationNode, FBEvaluateInfo *pEvaluateInfo )
{
	double		lW, lR;
	FBVector3d	lV;
	bool			lStatus[2];

	// Read connector in values
	lStatus[0] = mWeight	->ReadData( &lW, pEvaluateInfo );
	lStatus[1] = mInherit	->ReadData( lV, pEvaluateInfo );

	// Set default values if no input connection.
	if( !lStatus[0] )
	{
		lW = 0.0;
	}
	if( !lStatus[1] )
	{
		lV[1] = 1.0;
	}

	// Calculate result.
	lR	= 100.0f - fabs(lW);
	lR	*= lV[1]*0.01;
	lR	*= 0.01;

	// Write result out to connector.
	mResult->WriteData( &lR, pEvaluateInfo );
	return true;
}


/************************************************
 *	FBX Storage.
 ************************************************/
bool CHandWeight2::FbxStore( FBFbxObject *pFbxObject, kFbxObjectStore pStoreWhat )
{
	/*
	*	Store box parameters.
	*/
	return true;
}


/************************************************
 *	FBX Retrieval.
 ************************************************/
bool CHandWeight2::FbxRetrieve(FBFbxObject *pFbxObject, kFbxObjectStore pStoreWhat )
{
	/*
	*	Retrieve box parameters.
	*/
	return true;
}


//------------------------------------------------------------------------------------- WEIGHT 3


/************************************************
 *	Creation
 ************************************************/
bool CHandWeight3::FBCreate()
{
	if( FBBox::FBCreate() )
	{
		// Input Nodes
		mWeight	= AnimationNodeInCreate	( 0, "Weight", ANIMATIONNODE_TYPE_NUMBER );
		mInherit= AnimationNodeInCreate	( 0, "Inherit", ANIMATIONNODE_TYPE_VECTOR );
	
		// Output Node
		mResult		= AnimationNodeOutCreate( 3, "Result",	ANIMATIONNODE_TYPE_NUMBER );

		return true;
	}
	return false;
}


/************************************************
 *	Destruction.
 ************************************************/
void CHandWeight3::FBDestroy()
{
	FBBox::FBDestroy();
}


/************************************************
 *	Real-time engine evaluation
 ************************************************/
bool CHandWeight3::AnimationNodeNotify( FBAnimationNode *pAnimationNode, FBEvaluateInfo *pEvaluateInfo )
{
	double		lW, lR;
	FBVector3d	lV;
	bool			lStatus[2];

	// Read connector in values
	lStatus[0] = mWeight	->ReadData( &lW, pEvaluateInfo );
	lStatus[1] = mInherit	->ReadData( lV, pEvaluateInfo );

	// Set default values if no input connection.
	if( !lStatus[0] )
	{
		lW = 0.0;
	}
	if( !lStatus[1] )
	{
		lV[2] = 1.0;
	}

	// Calculate result.
	lR = (lW < 0.0) ? 100.0f - fabs(lW) : 100.0;
	
	lR	*= lV[2]*0.01;
	lR	*= 0.01;

	// Write result out to connector.
	mResult->WriteData( &lR, pEvaluateInfo );
	return true;
}


/************************************************
 *	FBX Storage.
 ************************************************/
bool CHandWeight3::FbxStore( FBFbxObject *pFbxObject, kFbxObjectStore pStoreWhat )
{
	/*
	*	Store box parameters.
	*/
	return true;
}


/************************************************
 *	FBX Retrieval.
 ************************************************/
bool CHandWeight3::FbxRetrieve(FBFbxObject *pFbxObject, kFbxObjectStore pStoreWhat )
{
	/*
	*	Retrieve box parameters.
	*/
	return true;
}