<?php
/**
 * TOP API: taobao.tbk.sc.newuser.order.sum request
 * 
 * @author auto create
 * @since 1.0, 2019.04.23
 */
class TbkScNewuserOrderSumRequest
{
	/** 
	 * 活动ID，现有活动id包括： 2月手淘拉新：119013_2；3月手淘拉新：119013_3；
	 **/
	private $activityId;
	
	/** 
	 * mm_xxx_xxx_xxx的第三位
	 **/
	private $adzoneId;
	
	/** 
	 * 页码，默认1
	 **/
	private $pageNo;
	
	/** 
	 * 页大小，默认20，1~100
	 **/
	private $pageSize;
	
	/** 
	 * 结算月份，需按照活动的结算月份传入具体的值：201807
	 **/
	private $settleMonth;
	
	/** 
	 * mm_xxx_xxx_xxx的第二位
	 **/
	private $siteId;
	
	private $apiParas = array();
	
	public function setActivityId($activityId)
	{
		$this->activityId = $activityId;
		$this->apiParas["activity_id"] = $activityId;
	}

	public function getActivityId()
	{
		return $this->activityId;
	}

	public function setAdzoneId($adzoneId)
	{
		$this->adzoneId = $adzoneId;
		$this->apiParas["adzone_id"] = $adzoneId;
	}

	public function getAdzoneId()
	{
		return $this->adzoneId;
	}

	public function setPageNo($pageNo)
	{
		$this->pageNo = $pageNo;
		$this->apiParas["page_no"] = $pageNo;
	}

	public function getPageNo()
	{
		return $this->pageNo;
	}

	public function setPageSize($pageSize)
	{
		$this->pageSize = $pageSize;
		$this->apiParas["page_size"] = $pageSize;
	}

	public function getPageSize()
	{
		return $this->pageSize;
	}

	public function setSettleMonth($settleMonth)
	{
		$this->settleMonth = $settleMonth;
		$this->apiParas["settle_month"] = $settleMonth;
	}

	public function getSettleMonth()
	{
		return $this->settleMonth;
	}

	public function setSiteId($siteId)
	{
		$this->siteId = $siteId;
		$this->apiParas["site_id"] = $siteId;
	}

	public function getSiteId()
	{
		return $this->siteId;
	}

	public function getApiMethodName()
	{
		return "taobao.tbk.sc.newuser.order.sum";
	}
	
	public function getApiParas()
	{
		return $this->apiParas;
	}
	
	public function check()
	{
		
		RequestCheckUtil::checkNotNull($this->activityId,"activityId");
		RequestCheckUtil::checkNotNull($this->pageNo,"pageNo");
		RequestCheckUtil::checkNotNull($this->pageSize,"pageSize");
		RequestCheckUtil::checkMaxValue($this->pageSize,100,"pageSize");
		RequestCheckUtil::checkMinValue($this->pageSize,1,"pageSize");
	}
	
	public function putOtherTextParam($key, $value) {
		$this->apiParas[$key] = $value;
		$this->$key = $value;
	}
}
