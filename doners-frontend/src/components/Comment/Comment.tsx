import Avatar from 'assets/theme/Avatar/Avatar';
import classNames from 'classnames/bind';
import styles from './Comment.module.scss';
import editIcon from 'assets/images/icon/edit.svg';
import deleteIcon from 'assets/images/icon/delete.svg';
import H4 from 'assets/theme/Typography/H4/H4';
import Span from 'assets/theme/Typography/Span/Span';
import CustomButton from 'assets/theme/Button/CustomButton/CustomButton';
import { deleteComments, modifyComment } from 'services/api/Comment';
import { useCallback, useEffect, useRef, useState } from 'react';
import { getUserProfile } from 'services/api/UserApi';
import { Link } from 'react-router-dom';
import { fToNow } from 'utils/formatTime';

const cx = classNames.bind(styles);
const Comment = ({ id, date, content, nickname, onDelete, onModify }: any) => {
  const [isOwn, setIsOwn] = useState(false);
  const [imgSrc, setImgSrc] = useState('');
  const [comment, setComment] = useState('');
  const [isDisabled, setIsDisabled] = useState(true);

  const commentRef = useRef<HTMLTextAreaElement>(null);
  const handleDeleteClick = () => {
    delComment();
  };

  const delComment = async () => {
    await deleteComments(id);
    onDelete(id);
  };

  const checkUser = useCallback(() => {
    const user = sessionStorage.getItem('user');
    if (typeof user === 'string') {
      const Juser = JSON.parse(user);
      if (nickname === Juser.nickName) {
        setIsOwn(true);
      }
    }
  }, [nickname]);

  const getProfileImg = async () => {
    const response = await getUserProfile(nickname);
    if (response) {
      // 이미지등록
      setImgSrc(response.data.profileImage);
    }
  };

  const handleModifyClick = () => {
    setIsDisabled(false);
  };

  const modComment = async () => {
    const body = {
      commentId: id,
      commentDescription: comment,
    };
    const result = await modifyComment(body);
    onModify();
  };

  const handleCancelClick = () => {
    setIsDisabled(true);
  };

  const handleModifyCompleteClick = () => {
    //api 호출
    modComment();
    setIsDisabled(true);
  };

  useEffect(() => {
    checkUser();
    getProfileImg();
    setComment(content);
  }, []);

  const handleInputChange = () => {
    if (commentRef.current) {
      setComment(commentRef.current.value);
    }
  };

  return (
    <div className={cx('inner-container')}>
      <div className={cx('comment-header')}>
        <div className={cx('comment-info')}>
          <Link to={`/profile/${nickname}`}>
            <Avatar src={imgSrc} />
          </Link>
          <div className={cx('sub-info')}>
            <H4>{nickname}</H4>
            <Span>{fToNow(date)}</Span>
            {/* <Span>{date}</Span> */}
          </div>
        </div>
        <div className={cx('button-wrap')}>
          {isOwn && isDisabled ? (
            <div className={cx('buttons')}>
              <CustomButton
                src={editIcon}
                color="yellow"
                size="small"
                onClick={handleModifyClick}
              >
                수정
              </CustomButton>
              <CustomButton
                src={deleteIcon}
                size="small"
                onClick={handleDeleteClick}
              >
                삭제
              </CustomButton>
            </div>
          ) : null}
        </div>
      </div>
      <div className={cx('comment-form')}>
        <textarea
          className={cx('comment-input', { disabled: isDisabled })}
          ref={commentRef}
          onChange={handleInputChange}
          value={comment}
          disabled={isDisabled}
        />
        {!isDisabled ? (
          <div className={cx('modify-completeBtn')}>
            <div className={cx('buttons')}>
              <CustomButton
                src={editIcon}
                color="yellow"
                size="small"
                onClick={handleModifyCompleteClick}
              >
                수정완료
              </CustomButton>
              <CustomButton
                src={deleteIcon}
                size="small"
                onClick={handleCancelClick}
              >
                취소
              </CustomButton>
            </div>
          </div>
        ) : null}
        {/* <Input value={content} /> */}
        {/* value={content} /> */}
      </div>
    </div>
  );
};

export default Comment;
