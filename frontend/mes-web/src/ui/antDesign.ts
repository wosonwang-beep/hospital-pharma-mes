import type { App } from 'vue'
import {
  Alert, Avatar, Breadcrumb, Button, Card, Checkbox, Empty, Form, Input,
  Layout, Menu, Pagination, Popconfirm, Select, Space, Table, Tag
} from 'ant-design-vue'

export function registerAntDesign(app: App): App {
  return app.use(Alert).use(Avatar).use(Breadcrumb).use(Button).use(Card)
    .use(Checkbox).use(Empty).use(Form).use(Input).use(Layout).use(Menu)
    .use(Pagination).use(Popconfirm).use(Select).use(Space).use(Table).use(Tag)
}
