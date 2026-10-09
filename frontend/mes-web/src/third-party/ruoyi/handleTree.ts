/** Adapted from RuoYi-Vue3 src/utils/ruoyi.js handleTree, MIT (c) 2018 RuoYi.
 * Upstream commit 838965c5a18d2c61b73ec30c6e288057aaa08b63.
 * Uses typed keys, Map, and copies so callers retain immutable source records.
 */
export type TreeNode<T> = T & {children:TreeNode<T>[]}
export function handleTree<T extends {id:string;parentId:string|null}>(data:T[]):TreeNode<T>[] {
 const childrenListMap=new Map<string,TreeNode<T>>()
 const tree:TreeNode<T>[]=[]
 for(const item of data) childrenListMap.set(item.id,{...item,children:[]})
 for(const item of data){const node=childrenListMap.get(item.id)!;const parent=item.parentId?childrenListMap.get(item.parentId):undefined;if(!parent)tree.push(node);else parent.children.push(node)}
 return tree
}
