import classNames from 'classnames/bind';
import styles from './ApprovalModal.module.scss';
const cx = classNames.bind(styles);

const ApprovalModal = () => {
  return (
    <div className={cx('card')}>
      <div className={cx('p')}>
        모달
      </div>
    </div>
  )

}

export default ApprovalModal;