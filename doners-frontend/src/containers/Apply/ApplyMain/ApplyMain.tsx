import Button from 'assets/theme/Button/Button';
import React from 'react';
import classNames from 'classnames/bind';
import styles from './ApplyMain.module.scss';
import H3 from 'assets/theme/Typography/H3/H3';
import { Link, useNavigate } from 'react-router-dom';
import Pointer from 'assets/images/apply-main-pointer.png';
import Charactor from 'assets/images/character-coin-yellow.png';
import { checkUserFundState } from 'services/api/Donation';
const cx = classNames.bind(styles);
const ApplyMain = () => {
  const navigate = useNavigate();
  const applyPageMove = async () => {
    //api 판단해서
    try {
      const result = await checkUserFundState();
      if (result.data.check) {
        navigate('/apply/fail');
      } else {
        navigate('/apply/form');
      }
    } catch (error) {
      console.log(error);
    }
  };

  return (
    <section className={cx('container')}>
      <div className={cx('row')}>
        <div className={cx('col-lg-12')}>
          <div className={cx('inner-container')}>
            <header className={cx('header')}>
              <div>모금 신청하기</div>
            </header>
            <main className={cx('content')}>
              <div>도움이 필요하신가요? 도너스에서 손을 내밀어보세요.</div>
              <div>
                <img
                  className={cx('charactor')}
                  src={Charactor}
                  alt="character"
                />
                <img className={cx('pointer')} src={Pointer} alt="pointer" />
              </div>
            </main>
            <footer className={cx('footer')}>
              <Button color={'primary'} onClick={applyPageMove}>
                모금 신청하기
              </Button>
            </footer>
          </div>
        </div>
      </div>
    </section>
  );
};

export default ApplyMain;
