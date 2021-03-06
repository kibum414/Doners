import { Link } from 'react-router-dom';
import classNames from 'classnames/bind';
import styles from './FundHistory.module.scss';
import { useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import FundingItem from 'components/FundingItem/FundingItem';
import LastFundingItem from 'components/LastFundingItem/LastFundingItem';
import H3 from 'assets/theme/Typography/H3/H3';
import { getUserApplicationList } from 'services/api/UserApi';
import { ApplicationProfileListType } from 'types/ApplicationTypes';
import { getLoggedUserNickname } from 'utils/loggedUser';

const cx = classNames.bind(styles);

type FundHistoryType = {
  nickname: string;
};

const FundHistory = ({ nickname }: FundHistoryType) => {
  const [applicationList, setApplicationList] = useState<
    ApplicationProfileListType[]
  >([]);

  /* 기부 신청 목록 조회 */
  const getApplicationList = async () => {
    const response = await getUserApplicationList();
    setApplicationList(response.data.userMyPageDonationHistoryResponseDTOList);
  };

  useEffect(() => {
    getApplicationList();
  }, [nickname]);

  return (
    <div>
      {applicationList?.length > 0 ? (
        <div>
          <H3>신청한 모금</H3>
          <FundingItem
            item={applicationList[0]}
            isOwner={nickname === getLoggedUserNickname()}
          />
        </div>
      ) : (
        <div>진행 중인 기부가 없습니다</div>
      )}
      {applicationList?.length > 0 ? (
        <>
          <div className={cx('divider')}>
            <hr />
          </div>
          <H3>이전 모금 내역</H3>
          {applicationList.length > 1
            ? applicationList?.map((item, idx) => {
                if (idx !== 0) {
                  return (
                    <div key={idx}>
                      <LastFundingItem
                        item={item}
                        isOwner={nickname === getLoggedUserNickname()}
                      />
                    </div>
                  );
                }
              })
            : null}
        </>
      ) : null}
    </div>
  );
};

export default FundHistory;
