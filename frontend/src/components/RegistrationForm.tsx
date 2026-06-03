import React from 'react';
import { Form, Input, Radio, Button, message } from 'antd';
import { registrationApi } from '../api';
import type { RegistrationRequest } from '../types';

interface RegistrationFormProps {
  activityId: number;
  onSuccess?: () => void;
}

const RegistrationForm: React.FC<RegistrationFormProps> = ({ activityId, onSuccess }) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = React.useState(false);

  const handleSubmit = async (values: RegistrationRequest) => {
    setLoading(true);
    try {
      await registrationApi.register(activityId, values);
      message.success('报名成功！');
      form.resetFields();
      onSuccess?.();
    } catch (err: any) {
      message.error(err.response?.data?.message || '报名失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Form
      form={form}
      layout="vertical"
      onFinish={handleSubmit}
      initialValues={{ preferredArea: 'ANY' }}
    >
      <Form.Item name="studentId" label="学号" rules={[{ required: true, message: '请输入学号' }]}>
        <Input placeholder="请输入学号" />
      </Form.Item>

      <Form.Item name="studentName" label="姓名" rules={[{ required: true, message: '请输入姓名' }]}>
        <Input placeholder="请输入姓名" />
      </Form.Item>

      <Form.Item name="preferredArea" label="偏好区域">
        <Radio.Group>
          <Radio value="FRONT">前排</Radio>
          <Radio value="BACK">后排</Radio>
          <Radio value="WINDOW">靠窗</Radio>
          <Radio value="ANY">不限</Radio>
        </Radio.Group>
      </Form.Item>

      <Form.Item name="teamName" label="团队名称（可选，同一团队会安排连座）">
        <Input placeholder="留空则为个人报名" />
      </Form.Item>

      <Form.Item>
        <Button type="primary" htmlType="submit" loading={loading} block>
          提交报名
        </Button>
      </Form.Item>
    </Form>
  );
};

export default RegistrationForm;
