#ifndef OBJTOOLS_FORMAT_ITEMS___GAP_ITEM__HPP
#define OBJTOOLS_FORMAT_ITEMS___GAP_ITEM__HPP

/*  $Id: gap_item.hpp 340218 2011-10-06 13:00:57Z kornbluh $
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
* Author:  Mati Shomrat
*
* File Description:
*   Indicate a gap region on a bioseq
*   
*
*/
#include <corelib/ncbistd.hpp>
#include <objtools/format/items/item_base.hpp>


BEGIN_NCBI_SCOPE
BEGIN_SCOPE(objects)


class CBioseqContext;
class IFormatter;


///////////////////////////////////////////////////////////////////////////
//
// SEQUENCE

class NCBI_FORMAT_EXPORT CGapItem : public CFlatItem
{
public:

    typedef std::vector<std::string> TEvidence;

    // constructors
    CGapItem( TSeqPos from, TSeqPos to, CBioseqContext& ctx, 
        const string &sFeatureName, // e.g. "gap" or "assembly_gap"
        const string &sType,
        const TEvidence &sEvidence,
        TSeqPos estimated_length = kInvalidSeqPos // unknown length by default
    );

    void Format(IFormatter& formatter, IFlatTextOStream& text_os) const;
    
    TSeqPos GetFrom(void) const;
    TSeqPos GetTo(void) const;

    const std::string & GetFeatureName(void) const;

    bool HasType() const;
    const std::string & GetType(void) const;

    bool HasEvidence() const;
    const TEvidence & GetEvidence(void) const;

    bool HasEstimatedLength(void) const;
    TSeqPos GetEstimatedLength(void) const;

private:
    void x_GatherInfo(CBioseqContext& ctx) {}

    // data
    TSeqPos     m_From, m_To;
    TSeqPos     m_EstimatedLength;
    std::string m_sFeatureName;
    std::string m_sType;
    TEvidence m_sEvidence;
};


//===========================================================================
//                              inline methods
//===========================================================================

inline
TSeqPos CGapItem::GetFrom(void) const
{
    return m_From;
}

inline
TSeqPos CGapItem::GetTo(void) const
{
    return m_To;
}

inline
const std::string & CGapItem::GetFeatureName(void) const
{
    return m_sFeatureName;
}

inline
bool CGapItem::HasType() const
{
    return ! m_sType.empty();
}

inline
const std::string & CGapItem::GetType(void) const
{
    return m_sType;
}

inline
bool CGapItem::HasEvidence() const
{
    return ! m_sEvidence.empty();
}

inline
const CGapItem::TEvidence & CGapItem::GetEvidence(void) const
{
    return m_sEvidence;
}

inline
bool CGapItem::HasEstimatedLength(void) const
{
    return m_EstimatedLength != kInvalidSeqPos;
}

inline
TSeqPos CGapItem::GetEstimatedLength(void) const
{
    return m_EstimatedLength;
}

END_SCOPE(objects)
END_NCBI_SCOPE

#endif  /* OBJTOOLS_FORMAT_ITEMS___GAP_ITEM__HPP */
