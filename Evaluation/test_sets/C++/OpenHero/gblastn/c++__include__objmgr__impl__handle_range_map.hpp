#ifndef OBJECTS_OBJMGR_IMPL___HANDLE_RANGE_MAP__HPP
#define OBJECTS_OBJMGR_IMPL___HANDLE_RANGE_MAP__HPP

/*  $Id: handle_range_map.hpp 252199 2011-02-14 14:11:26Z vasilche $
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
 * Author: Aleksey Grichenko, Michael Kimelman, Eugene Vasilchenko
 *
 * File Description:
 *
 */

#include <objmgr/impl/handle_range.hpp>
#include <objects/seq/seq_id_handle.hpp>
#include <corelib/ncbiobj.hpp>
#include <map>
#include <vector>
#include <list>

BEGIN_NCBI_SCOPE
BEGIN_SCOPE(objects)

class CSeq_loc;
class CSeqMap;
class CBioseq_Info;

class CMasterSeqSegments : public CObject {
public:
    CMasterSeqSegments(void);
    CMasterSeqSegments(const CBioseq_Info& seq);
    ~CMasterSeqSegments(void);

    void AddSegments(const CSeqMap& seq);
    int AddSegment(const CSeq_id_Handle& id, bool minus_strand);
    void AddSegmentId(int idx, const CSeq_id_Handle& id);

    typedef vector<CSeq_id_Handle> TIds;
    typedef list< CRef<CSeq_id> > TIds2;
    void AddSegmentIds(int idx, const TIds& ids);
    void AddSegmentIds(int idx, const TIds2& ids);
    void AddSegmentIds(const TIds& ids);
    void AddSegmentIds(const TIds2& ids);

    size_t GetSegmentCount(void) const {
        return m_SegSet.size();
    }
    int FindSeg(const CSeq_id_Handle& h) const;
    bool GetMinusStrand(int seg) const;
    const CSeq_id_Handle& GetHandle(int seg) const;

protected:
    typedef pair<CSeq_id_Handle, bool> TSeg;
    typedef vector<TSeg> TSegSet;
    typedef map<CSeq_id_Handle, int> TId2Seg;

    TSegSet m_SegSet;
    TId2Seg m_Id2Seg;
                       
private:
    CMasterSeqSegments(const CMasterSeqSegments&);
    void operator=(const CMasterSeqSegments&);
};

// Seq_loc substitution for internal use by iterators and data sources
class NCBI_XOBJMGR_EXPORT CHandleRangeMap
{
public:
    typedef CHandleRange::TRange TRange;
    typedef map<CSeq_id_Handle, CHandleRange> TLocMap;
    typedef TLocMap::const_iterator const_iterator;

    CHandleRangeMap(void);
    CHandleRangeMap(const CHandleRangeMap& rmap);
    ~CHandleRangeMap(void);

    CHandleRangeMap& operator= (const CHandleRangeMap& rmap);

    struct SAddState {
        typedef CHandleRange::TRange TRange;

        CSeq_id_Handle  m_PrevId;
        ENa_strand      m_PrevStrand;
        TRange          m_PrevRange;
    };
    
    // Add all ranges for each seq-id from a seq-loc
    void AddLocation(const CSeq_loc& loc);
    void AddLocation(const CSeq_loc& loc, SAddState& state);
    // Add range substituting with handle "h"
    void AddRange(const CSeq_id_Handle& h,
                  const TRange& range, ENa_strand strand);
    // Add ranges from "range" with handle "h"
    void AddRanges(const CSeq_id_Handle& h, const CHandleRange& hr);
    CHandleRange& AddRanges(const CSeq_id_Handle& h);

    // Get the ranges map
    const TLocMap& GetMap(void) const { return m_LocMap; }
    bool empty(void) const { return m_LocMap.empty(); }

    void clear(void);

    // iterate
    const_iterator begin(void) const { return m_LocMap.begin(); }
    const_iterator find(const CSeq_id_Handle& idh) const {
        return m_LocMap.find(idh);
    }
    const_iterator end(void) const { return m_LocMap.end(); }

    bool IntersectingWithLoc(const CSeq_loc& loc) const;
    bool IntersectingWithMap(const CHandleRangeMap& rmap) const;
    bool TotalRangeIntersectingWith(const CHandleRangeMap& rmap) const;

    void AddRange(const CSeq_id& id, TSeqPos from, TSeqPos to,
                  ENa_strand strand = eNa_strand_unknown);
    void AddRange(const CSeq_id& id,
                  const TRange& range, ENa_strand strand = eNa_strand_unknown);
    
    void AddRange(const CSeq_id_Handle& h,
                  const TRange& range, ENa_strand strand,
                  SAddState& state);
    void AddRange(const CSeq_id& id,
                  const TRange& range, ENa_strand strand,
                  SAddState& state);
    void AddRange(const CSeq_id& id,
                  TSeqPos from, TSeqPos to, ENa_strand strand,
                  SAddState& state);

    void SetMasterSeq(const CMasterSeqSegments* master_seq) {
        m_MasterSeq = master_seq;
    }

private:
    // Split the location and add range lists to the locmap
    void x_ProcessLocation(const CSeq_loc& loc);

    TLocMap m_LocMap;
    CConstRef<CMasterSeqSegments> m_MasterSeq;
};


END_SCOPE(objects)
END_NCBI_SCOPE

#endif  // OBJECTS_OBJMGR_IMPL___HANDLE_RANGE_MAP__HPP
