import classNames from 'classnames/bind';
import styles from './DashboardCard.module.scss';

const cx = classNames.bind(styles);

type DashboardCardType = {
  header: string;
  content: string;
  label: string;
};

const DashboardCard = ({ header, content, label }: DashboardCardType) => {
  return (
    <div className={cx('card')}>
      <div className={cx('card-header')}>{header}</div>
      <span className={cx('card-content')}>
        {content}
        {/* <H2 >{content.toString()}</H2>
        <P>{label}</P> */}
      </span>
      <span className={cx('card-content-label')}>{label}</span>
    </div>
  );
};

export default DashboardCard;
