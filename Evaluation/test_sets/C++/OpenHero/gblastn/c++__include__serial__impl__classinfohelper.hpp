#ifndef CLASSINFOHELPER__HPP
#define CLASSINFOHELPER__HPP

/*  $Id: classinfohelper.hpp 348915 2012-01-05 17:03:37Z vasilche $
* ===========================================================================
*
*                            PUBLIC DOMAIN NOTICE
*               National Center for Biotechnology Information
*
*  This software/database is a "United States Government Work" under the
*  terms of the United States Copyright Act.  It was written as part of
*  the author's official duties as a United States Government employee and
*  thus cannot be copyrighted.  This software/database is freely available
*  to the public for use. The National Library of Medicine and the U.S.
*  Government have not placed any restriction on its use or reproduction.
*
*  Although all reasonable efforts have been taken to ensure the accuracy
*  and reliability of the software and data, the NLM and the U.S.
*  Government do not and cannot warrant the performance or results that
*  may be obtained by using this software or data. The NLM and the U.S.
*  Government disclaim all warranties, express or implied, including
*  warranties of performance, merchantability or fitness for any particular
*  purpose.
*
*  Please cite the author in any work or product based on this material.
*
* ===========================================================================
*
* Author: Eugene Vasilchenko
*
* File Description:
*   !!! PUT YOUR DESCRIPTION HERE !!!
*/

#include <corelib/ncbistd.hpp>
#include <serial/serialbase.hpp>
#include <serial/impl/typeinfoimpl.hpp>
#include <typeinfo>


/** @addtogroup GenClassSupport
 *
 * @{
 */


BEGIN_NCBI_SCOPE

class CClassTypeInfoBase;
class CClassTypeInfo;
class CChoiceTypeInfo;

// these methods are external to avoid inclusion of big headers
class NCBI_XSERIAL_EXPORT CClassInfoHelperBase
{
protected:
    typedef const type_info* (*TGetTypeIdFunction)(TConstObjectPtr object);
    typedef CTypeInfo::TTypeCreate TCreateFunction; 
    typedef TMemberIndex (*TWhichFunction)(const CChoiceTypeInfo* choiceType,
                                           TConstObjectPtr choicePtr);
    typedef void (*TResetFunction)(const CChoiceTypeInfo* choiceType,
                                   TObjectPtr choicePtr);
    typedef void (*TSelectFunction)(const CChoiceTypeInfo* choiceType,
                                    TObjectPtr choicePtr,
                                    TMemberIndex index,
                                    CObjectMemoryPool* memPool);
    typedef void (*TSelectDelayFunction)(TObjectPtr object,
                                         TMemberIndex index);

    static CChoiceTypeInfo* CreateChoiceInfo(const char* name, size_t size,
                                             const void* nonCObject,
                                             TCreateFunction createFunc,
                                             const type_info& ti,
                                             TWhichFunction whichFunc,
                                             TSelectFunction selectFunc,
                                             TResetFunction resetFunc);
    static CChoiceTypeInfo* CreateChoiceInfo(const char* name, size_t size,
                                             const CObject* cObject,
                                             TCreateFunction createFunc,
                                             const type_info& ti,
                                             TWhichFunction whichFunc,
                                             TSelectFunction selectFunc,
                                             TResetFunction resetFunc);

public:
#if HAVE_NCBI_C
    static CChoiceTypeInfo* CreateAsnChoiceInfo(const char* name);
    static CClassTypeInfo* CreateAsnStructInfo(const char* name, size_t size,
                                               const type_info& id);
#endif
    
protected:
    static CClassTypeInfo* CreateClassInfo(const char* name, size_t size,
                                           const void* nonCObject,
                                           TCreateFunction createFunc,
                                           const type_info& id,
                                           TGetTypeIdFunction func);
    static CClassTypeInfo* CreateClassInfo(const char* name, size_t size,
                                           const CObject* cObject,
                                           TCreateFunction createFunc,
                                           const type_info& id,
                                           TGetTypeIdFunction func);
};

// template collecting all helper methods for generated classes
template<class C>
class CClassInfoHelper : public CClassInfoHelperBase
{
    typedef CClassInfoHelperBase CParent;
public:
    typedef C CClassType;

    static CClassType& Get(void* object)
        {
            return *static_cast<CClassType*>(object);
        }
    static const CClassType& Get(const void* object)
        {
            return *static_cast<const CClassType*>(object);
        }

    static void* Create(TTypeInfo /*typeInfo*/,
                        CObjectMemoryPool* /*memPool*/)
        {
            return new CClassType();
        }
    static void* CreateCObject(TTypeInfo /*typeInfo*/,
                               CObjectMemoryPool* memPool)
        {
            return new(memPool) CClassType();
        }


    static const type_info* GetTypeId(const void* object)
        {
            const CClassType& x = Get(object);
            return &typeid(x);
        }

    enum EGeneratedChoiceValues {
        eGeneratedChoiceEmpty = 0,
        eGeneratedChoiceToMemberIndex = kEmptyChoice - eGeneratedChoiceEmpty,
        eMemberIndexToGeneratedChoice = eGeneratedChoiceEmpty - kEmptyChoice
    };

    static TMemberIndex WhichChoice(const CChoiceTypeInfo* /*choiceType*/,
                                    const void* choicePtr)
        {
            return static_cast<TMemberIndex>(Get(choicePtr).Which())
                + eGeneratedChoiceToMemberIndex;
        }
    static void ResetChoice(const CChoiceTypeInfo* choiceType,
                            void* choicePtr)
        {
            if ( WhichChoice(choiceType, choicePtr) != kEmptyChoice )
                Get(choicePtr).Reset();
        }
    static void SelectChoice(const CChoiceTypeInfo* choiceType,
                             void* choicePtr,
                             TMemberIndex index,
                             CObjectMemoryPool* memPool)
        {
            typedef typename CClassType::E_Choice E_Choice;
            if (WhichChoice(choiceType,choicePtr) != index) {
                Get(choicePtr).Select(E_Choice(index + eMemberIndexToGeneratedChoice), NCBI_NS_NCBI::eDoResetVariant, memPool);
            }
        }
    static void SelectDelayBuffer(void* choicePtr,
                                  TMemberIndex index)
        {
            typedef typename CClassType::E_Choice E_Choice;
            Get(choicePtr).SelectDelayBuffer(E_Choice(index + eMemberIndexToGeneratedChoice));
        }

    static void SetReadWriteMethods(NCBI_NS_NCBI::CClassTypeInfo* info)
        {
            const CClassType* object = 0;
            NCBISERSetPreRead(object, info);
            NCBISERSetPostRead(object, info);
            NCBISERSetPreWrite(object, info);
            NCBISERSetPostWrite(object, info);
        }
    static void SetReadWriteMethods(NCBI_NS_NCBI::CChoiceTypeInfo* info)
        {
            const CClassType* object = 0;
            NCBISERSetPreRead(object, info);
            NCBISERSetPostRead(object, info);
            NCBISERSetPreWrite(object, info);
            NCBISERSetPostWrite(object, info);
        }
    static void SetReadWriteMemberMethods(NCBI_NS_NCBI::CClassTypeInfo* info)
        {
            const CClassType* object = 0;
            NCBISERSetGlobalReadMemberHook(object, info);
            NCBISERSetGlobalReadVariantHook(object, info);
        }
    static void SetReadWriteVariantMethods(NCBI_NS_NCBI::CChoiceTypeInfo* info)
        {
            const CClassType* object = 0;
            NCBISERSetGlobalReadMemberHook(object, info);
            NCBISERSetGlobalReadVariantHook(object, info);
        }

    static CClassTypeInfo* CreateAbstractClassInfo(const char* name)
        {
            const CClassType* object = 0;
            CClassTypeInfo* info =
                CParent::CreateClassInfo(name, sizeof(CClassType),
                                         object, &CVoidTypeFunctions::Create,
                                         typeid(CClassType), &GetTypeId);
            SetReadWriteMethods(info);
            return info;
        }

    static CClassTypeInfo* CreateClassInfo(const char* name)
        {
            const CClassType* object = 0;
            CClassTypeInfo* info = CreateClassInfo(name, object);
            SetReadWriteMethods(info);
            return info;
        }

    static CChoiceTypeInfo* CreateChoiceInfo(const char* name)
        {
            const CClassType* object = 0;
            CChoiceTypeInfo* info = CreateChoiceInfo(name, object);
            SetReadWriteMethods(info);
            return info;
        }
#ifdef HAVE_NCBI_C
    static CClassTypeInfo* CreateAsnStructInfo(const char* name)
        {
            return CParent::CreateAsnStructInfo(name,
                                                sizeof(CClassType),
                                                typeid(CClassType));
        }
#endif

private:
    static CClassTypeInfo* CreateClassInfo(const char* name,
                                           const void* nonCObject)
        {
            return CParent::CreateClassInfo(name, sizeof(CClassType),
                                            nonCObject, &Create,
                                            typeid(CClassType), &GetTypeId);
        }
    static CClassTypeInfo* CreateClassInfo(const char* name,
                                           const CObject* cObject)
        {
            return CParent::CreateClassInfo(name, sizeof(CClassType),
                                            cObject, &CreateCObject,
                                            typeid(CClassType), &GetTypeId);
        }
    static CChoiceTypeInfo* CreateChoiceInfo(const char* name,
                                             const void* nonCObject)
        {
            return CParent::CreateChoiceInfo(name, sizeof(CClassType),
                                             nonCObject, &Create, 
                                             typeid(CClassType),
                                             &WhichChoice,
                                             &SelectChoice, &ResetChoice);
        }
    static CChoiceTypeInfo* CreateChoiceInfo(const char* name,
                                             const CObject* cObject)
        {
            return CParent::CreateChoiceInfo(name, sizeof(CClassType),
                                             cObject, &CreateCObject, 
                                             typeid(CClassType),
                                             &WhichChoice,
                                             &SelectChoice, &ResetChoice);
        }
};


/* @} */


END_NCBI_SCOPE

#endif  /* CLASSINFOHELPER__HPP */
