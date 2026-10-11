import {api,type Page} from './http'
export interface CatalogField {fieldId:string;label:string;dataType:string;snapshotSemantic:string}
export interface KnownRelation {relationId:string;targetCatalogId:string;parentCatalogId:string;cardinality:string;keys:{parentFieldId:string;targetFieldId:string}[]}
export interface Catalog {catalogId:string;label:string;fields:CatalogField[];relations:KnownRelation[];authorizationPolicyRefs:string[]}
export interface Metadata {versionNo:number;authorizationPolicyRef:string;compatibleMainCatalogIds:string[];fixedTypeFilters:{catalogId:string;catalogFieldId:string;operator:'EQ';value:unknown}[]}
export interface DocumentType {value:string;label:string;disabled:boolean;disabledReason:string|null;metadata:Metadata|null}
export interface Policy {policyRef:string;label:string;compatibleMainCatalogIds:string[];businessReadPermissions:string[]}
export interface SourceJoin {nodeId:string;label:string;parentNodeId:string;catalogId:string;knownRelationId:string;cardinality:string;keys:{parentFieldId:string;targetFieldId:string}[];datasetKey:string|null;displayOrder:number;sort:{catalogFieldId:string;direction:string}[]}
export interface SourceField {fieldId:string;fieldKey:string;label:string;dataType:string;sourcePath:{nodeId:string;catalogFieldId:string};dataGroup:string;datasetKey:string|null;available:boolean;format:{kind:string;scale:number|null;roundingMode:string|null;dateStyle:string|null};missing:string;defaultValue:unknown;displayOrder:number}
export interface Configuration {schemaVersion:1;sourceName:string;mainCatalogId:string;parameterDeclarations:{name:string;source:string;contextKey:string|null}[];joins:SourceJoin[];fields:SourceField[]}
export interface Revision {id:string;configHash:string;configuration:Configuration;revisionNo:number}
export interface Validation {id:string;result:string;invalidatedAt:string|null;expiresAt:string;typeResults?:{businessType:string;result:string;issues:{code:string;message:string}[]}[];previewIncluded?:boolean;preview?:{businessType:string;recordId:string;data:Record<string,unknown>;rawData:Record<string,unknown>}[]}
export interface DataSource {id:string;sourceCode:string;sourceName:string;status:string;deletedAt:string|null;versionNo:number;businessTypes:{businessType:string;label:string}[];draftRevisionId:string|null;publishedRevisionId:string|null;draft:Revision|null;published:Revision|null;validation:Validation|null;allowedActions:string[];dependencies:{label:string}[]}
export const emptyConfiguration=():Configuration=>({schemaVersion:1,sourceName:'',mainCatalogId:'',parameterDeclarations:[{name:'recordId',source:'DOCUMENT_ID',contextKey:null}],joins:[],fields:[]})
export const rawFormat=()=>({kind:'RAW',scale:null,roundingMode:null,dateStyle:null})
export const sourceList=(params:Record<string,unknown>)=>api<Page<DataSource>>({url:'/printing/data-sources',params})
export const sourceDetail=(id:string)=>api<DataSource>({url:`/printing/data-sources/${id}`})
export const sourceTypes=(sourceId?:string)=>api<DocumentType[]>({url:'/printing/data-sources/metadata/document-types',params:{sourceId}})
export const sourceCatalogs=()=>api<{entities:Catalog[];catalogVersion:string}>({url:'/printing/data-sources/metadata/catalogs'})
export const sourcePolicies=()=>api<Policy[]>({url:'/printing/data-sources/metadata/authorization-policies'})
export function sourceMutation<T>(path:string,method:'POST'|'PUT'|'DELETE',data:Record<string,unknown>,version?:number){return api<T>({url:path,method,data,headers:{'Idempotency-Key':crypto.randomUUID(),...(version===undefined?{}:{'If-Match':String(version)})}})}
