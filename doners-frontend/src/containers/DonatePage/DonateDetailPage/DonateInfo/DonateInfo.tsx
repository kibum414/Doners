import Progressbar from 'assets/theme/Progressbar/Progressbar';
import H2 from 'assets/theme/Typography/H2/H2';
import H3 from 'assets/theme/Typography/H3/H3';
import H4 from 'assets/theme/Typography/H4/H4';
import P from 'assets/theme/Typography/P/P';
import Span from 'assets/theme/Typography/Span/Span';
import classNames from 'classnames/bind';
import { useEffect, useState } from 'react';
import { fundraiserIsWithdraw, nowBalance } from 'services/blockchain/SsfApi';
import { DontationDetailType } from 'types/DonationTypes';
import { calcDday } from 'utils/formatTime';
import DonateContentModal from '../DonateContentModal/DonateContentModal';
// import { DonationDetailType } from '../DontateDetail/DonateDetail';
import styles from './DonateInfo.module.scss';

const cx = classNames.bind(styles);

type DonateInfoProps = {
  data: DontationDetailType;
};

const DonateInfo = ({ data }: DonateInfoProps) => {
  const [isOpen, setIsOpen] = useState(false);
  const [rate, setRate] = useState(0);
  const [collectedBalance, setCollectedBalance] = useState(0);
  const [openContentModal, setOpenContentModal] = useState(false);

  useEffect(() => {
    if (data.contractAddress) {
      checkWithdrawState();
    }
  }, [data]);

  const checkWithdrawState = async () => {
    // (완료) 모금액 수령이 완료되었는지 검사
    const response = await fundraiserIsWithdraw(data.contractAddress);
    setCollectedBalance(response.targetMoney);
  };
  useEffect(() => {
    const result = Math.floor((collectedBalance / data.targetAmount) * 100);
    setRate(result);
  }, [collectedBalance]);

  const handleHistoryListClick = () => {
    setIsOpen((prev) => !prev);
  };

  const handleOpenModal = () => {
    setOpenContentModal(true);
  };

  const handleOnClose = () => {
    setOpenContentModal(false);
  };

  return (
    <div className={cx('info-form')}>
      <div className={cx('title')}>
        <H3>모금 상세 정보</H3>
      </div>
      <div className={cx('date-wrap')}>
        <div className={cx('date-row')}>
          <div className={cx('date-title')}>
            <H4>신청일: </H4>
          </div>
          <H4>{data.startDate}</H4>
        </div>
        <div className={cx('date-row')}>
          <div className={cx('date-title')}>
            <H4>마감일: </H4>
          </div>
          <H4>{data.endDate}</H4>
          <H4 color="red">{String(calcDday(data.endDate))}</H4>
        </div>
        <div className={cx('value-row')}>
          <div className={cx('value-title')}>
            <H4>목표금액: </H4>
          </div>
          <H2>{String(data.targetAmount)}</H2>
          <H4>SSF</H4>
        </div>
      </div>
      {data.approvalStatusCode !== 'BEFORE_CONFIRMATION' ? (
        <div className={cx('progress-info')}>
          <Progressbar value={rate} />
          <div className={cx('progress-rate')}>
            <Span color="gray">모금액 달성률 : </Span>
            <P color="green">{String(rate).concat('%')}</P>
          </div>
        </div>
      ) : null}
      <div>
        <div className={cx('donate-plan')}>
          <div className={cx('detail-plan')}>
            <H4>신청자의 글</H4>
            <div className={cx('ocBtn')} onClick={handleOpenModal}>
              <H4 color="green">더보기</H4>
            </div>
          </div>
        </div>
        <div className={cx('donate-title')}>
          {/* <H3>모금액 활용계획</H3> */}
        </div>
        <div className={cx('donate-plan')}>
          <div className={cx('detail-plan')}>
            <H4>모금 상세 계획</H4>
            <div className={cx('ocBtn')} onClick={handleHistoryListClick}>
              <H4 color="green">{isOpen ? '닫기' : '펼치기'}</H4>
            </div>
          </div>
          {isOpen ? (
            <div className={cx('history-items')}>
              {data.budget.map((value) => {
                return (
                  <div className={cx('history-item')} key={value.sequence}>
                    <P>{value.plan}</P>
                    <div className={cx('value')}>
                      <P>{`${value.amount.toLocaleString()}KRW`}</P>
                    </div>
                  </div>
                );
              })}
            </div>
          ) : null}
          <DonateContentModal
            open={openContentModal}
            onClose={handleOnClose}
            contents={data.description}
            title={data.title}
          />
        </div>
      </div>
    </div>
  );
};

export default DonateInfo;
