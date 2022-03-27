import H1 from 'assets/theme/Typography/H1/H1';

import classNames from 'classnames/bind';
import styles from './EpilogueEditor.module.scss';
import Button from 'assets/theme/Button/Button';
import EditorForm from 'components/Editor/EditorForm';
import EpilogueEditorHeader from './EpilogueEditorHeader/EpilogueEditorHeader';
import TotalDonate from './TotalDonate/TotalDonate';
import ReceiptEditor from './ReceiptEditor/ReceiptEditor';

const cx = classNames.bind(styles);

function EpilogueEditor() {
  return (
    <section className={cx('container')}>
      <div className={cx('row')}>
        <div className={cx('col-lg-12')}>
          <div className={cx('header')}>
            <H1>모금에 대한 후기를 작성해주세요.</H1>
          </div>
          <div className={cx('inner-container')}>
            <EpilogueEditorHeader />
            <EditorForm />
            <div className={cx('donate-receipt')}>
              <div className={cx('total-donate')}>
                <TotalDonate />
              </div>
              <ReceiptEditor />
            </div>
            <div className={cx('btn-row')}>
              <div className={cx('regist-btn')}>
                <Button color="primary" fullWidth>
                  작성 완료
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

export default EpilogueEditor;
