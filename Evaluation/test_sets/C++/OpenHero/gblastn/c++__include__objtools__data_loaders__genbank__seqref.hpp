#ifndef SEQREF__HPP_INCLUDED
#define SEQREF__HPP_INCLUDED
/* */

/*  $Id: seqref.hpp 191200 2010-05-10 18:54:11Z vasilche $
* ===========================================================================
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
* ===========================================================================
*
*  Author:  Eugene Vasilchenko
*
*  File Description: Base data reader interface
*
*/

#include <corelib/ncbiobj.hpp>
#include <objtools/data_loaders/genbank/blob_id.hpp>
#include <utility>
#include <string>

BEGIN_NCBI_SCOPE
BEGIN_SCOPE(objects)

class NCBI_XREADER_EXPORT CSeqref : public CObject
{
public:
    typedef pair<pair<int, int>, int> TKeyByTSE;

    enum EBlobFlags {
        fPossible    = 1 << 16,
        fPrivate     = 1 << 17
    };
    typedef TBlobContentsMask TFlags;

    enum ESat {
        eSat_ANNOT_CDD  = 10,
        eSat_ANNOT      = 26,
        eSat_TRACE      = 28,
        eSat_TRACE_ASSM = 29,
        eSat_TR_ASSM_CH = 30,
        eSat_TRACE_CHGR = 31
    };

    enum ESubSat {
        eSubSat_main =    0,
        eSubSat_SNP  = 1<<0,
        eSubSat_SNP_graph  = 1<<2,
        eSubSat_CDD  = 1<<3,
        eSubSat_MGC  = 1<<4,
        eSubSat_HPRD = 1<<5,
        eSubSat_STS  = 1<<6,
        eSubSat_tRNA = 1<<7,
        eSubSat_microRNA = 1<<8,
        eSubSat_Exon = 1<<9
    };
    typedef int TSubSat;

    CSeqref(void);
    CSeqref(int gi, int sat, int satkey);
    CSeqref(int gi, int sat, int satkey, TSubSat subsat, TFlags flags);
    virtual ~CSeqref(void);
    
    const string print(void)    const;
    const string printTSE(void) const;
    static const string printTSE(const TKeyByTSE& key);

    int GetGi() const
        {
            return m_Gi;
        }
    int GetSat() const
        {
            return m_Sat;
        }
    int GetSubSat() const
        {
            return m_SubSat;
        }
    int GetSatKey() const
        {
            return m_SatKey;
        }

    TKeyByTSE GetKeyByTSE(void) const
        {
            return TKeyByTSE(pair<int, int>(m_Sat, m_SubSat), m_SatKey);
        }

    bool SameTSE(const CSeqref& seqRef) const
        {
            return
                m_Sat == seqRef.m_Sat &&
                m_SubSat == seqRef.m_SubSat &&
                m_SatKey == seqRef.m_SatKey;
        }
    bool SameSeq(const CSeqref& seqRef) const
        {
            return m_Gi == seqRef.m_Gi && SameTSE(seqRef);
        }
    bool LessByTSE(const CSeqref& seqRef) const
        {
            if ( m_Sat != seqRef.m_Sat ) {
                return m_Sat < seqRef.m_Sat;
            }
            if ( m_SubSat != seqRef.m_SubSat ) {
                return m_SubSat < seqRef.m_SubSat;
            }
            return m_SatKey < seqRef.m_SatKey;
        }
    bool LessBySeq(const CSeqref& seqRef) const
        {
            if ( m_Gi != seqRef.m_Gi ) {
                return m_Gi < seqRef.m_Gi;
            }
            return LessByTSE(seqRef);
        }
    bool operator<(const CSeqref& seqRef) const
        {
            if ( GetSat() != seqRef.GetSat() )
                return GetSat() < seqRef.GetSat();
            if ( GetSatKey() != seqRef.GetSatKey() )
                return GetSatKey() < seqRef.GetSatKey();
            return GetVersion() < seqRef.GetVersion();
        }
    
    TFlags  GetFlags(void) const
        {
            return m_Flags;
        }
    void SetFlags(TFlags flags)
        {
            m_Flags = flags;
        }

    bool IsEmpty(void) const
        {
            return m_Sat < 0 && m_SatKey < 0;
        }

    bool IsMainBlob(void) const
        {
            return (GetFlags() & fBlobHasCore) != 0;
        }

    int GetVersion(void) const
        {
            return m_Version;
        }
    void SetVersion(int version)
        {
            m_Version = version;
        }

protected:
    TFlags  m_Flags;

    int m_Gi;
    int m_Sat;
    int m_SubSat; // external features mask
    int m_SatKey;
    int m_Version;
};


END_SCOPE(objects)
END_NCBI_SCOPE

#endif//SEQREF__HPP_INCLUDED
