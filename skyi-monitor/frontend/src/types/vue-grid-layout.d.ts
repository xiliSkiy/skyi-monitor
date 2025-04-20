declare module 'vue-grid-layout' {
  import { DefineComponent } from 'vue'
  
  export const GridLayout: DefineComponent<any, any, any>
  export const GridItem: DefineComponent<any, any, any>
  
  export const VueGridLayout: {
    GridLayout: DefineComponent<any, any, any>
    GridItem: DefineComponent<any, any, any>
  }
} 