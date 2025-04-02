
import { Component, OnInit} from '@angular/core';
import { ProductService } from '../../services/product.service';
import { CategoryService } from '../../services/category.service';
import { OrderService } from 'src/app/services/order.service';
import { Order } from 'src/app/models/order';
import { Location } from '@angular/common';
import { HttpParams } from '@angular/common/http';
import { UserService } from 'src/app/services/user.service';
import { AuthService } from 'src/app/services/auth.service';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss'],
  
})


export class AdminDashboardComponent {
  products: any[] = [];
  categories: any[] = [];
  newProduct: any = {};
  newCategory: any = {};
  selectedProduct: any = {};
  selectedCategory: any = {};
  productCountByCategory: Map<number, number> = new Map();
  isAddProductModalOpen: boolean = false;
  isAddCategoryModalOpen: boolean = false;
  isCreatingCategory: boolean = false;
  isUpdateProductModalOpen: boolean = false;
  isUpdateCategoryModalOpen: boolean = false;

  
  sortBy: string = 'sequId'; 
  sortOrder: string = 'desc'; 
  searchQuery: string = '';

  currentPage: number = 0;
  pageSize: number = 7;
  totalItems: number = 0;

  
  orderCurrentPage: number = 0;
  orderPageSize: number = 7;
  orderTotalItems: number = 0;

  // Filtri per gli ordini
  selectedStatus: string = '';
  startDate: string | null = null; 
  endDate: string | null = null; 
  orderSearchEmail: string = ''; 
  orderSortBy: string = 'createdAt'; 
  orderSortOrder: string = 'desc'; 
  dateRange: { start: Date | null; end: Date | null } = { start: null, end: null };
  

  orders: Order[] = [];
  totalOrders: number = 0;
  selectedOrder: any = {};
  isUpdateOrderModalOpen: boolean = false;
  newOrderStatus: string = '';

  countUser:number=0;
  countOrders:number=0;
  totalRevenue:number=0;
  counProducts:number=0;
  countCategories:number=0;

  salesData: any[] = [];
  chartData: any;
  chartOptions: any;

  constructor(
    private productService: ProductService,
    private categoryService: CategoryService,
    private orderService: OrderService,
    private userService: UserService,
    private authService: AuthService,
    private location: Location,
    

  ) { }

  ngOnInit(): void {

    

      this.loadProducts();
      this.loadCategories();
      this.loadOrders();
      this.loadProductCountByCategory();
      this.userCount();
      this.totalCountOrders();
      this.totalRevenueOrders();
      this.totalCountProducts();
      this.totalCountCategories();
      this.loadSalesData();
  
  

  }

  // Products Box

  loadProducts(): void {
    const sortParam = `${this.sortBy},${this.sortOrder}`;
    this.productService.getAllProductsPaginated(this.currentPage, this.pageSize, sortParam, this.searchQuery).subscribe((response: any) => {
      this.products = response.content;
      this.totalItems = response.totalElements;
    });
  }

  onPageChange(page: number): void {
    this.currentPage = page; 
    this.loadProducts(); 
  }

  onSortChange(field: string): void {
    if (this.sortBy === field) {
      
      this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
    } else {
      
      this.sortBy = field;
      this.sortOrder = 'asc';
    }
    this.loadProducts(); 
  }

  resetFilters(): void {    
    this.sortBy = 'sequId'; 
    this.sortOrder = 'desc'; 
    this.currentPage = 0; 
    this.searchQuery = ''; 
    this.loadProducts(); 
  }

  onSearch(): void {
    this.currentPage = 0; 
    this.loadProducts(); 
  }


  // Category Box

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe(data => {
      this.categories = data;
    });
  }

  loadProductCountByCategory(): void {
    this.categoryService.getProductCountByCategory().subscribe(countMap => {
      this.productCountByCategory = new Map(Object.entries(countMap).map(([key, value]) => [Number(key), value]));
    });
  }

  deleteProduct(productId: number): void {
    this.productService.deleteProduct(productId).subscribe(() => {
      this.loadProducts();
      this.loadProductCountByCategory();     
    });
  }

  deleteCategory(categoryId: number): void {
    this.categoryService.deleteCategory(categoryId).subscribe(() => {
      this.loadCategories();
    });
  }

  getCategoryTitle(categoryId: number): string {
    const category = this.categories.find(cat => cat.sequId === categoryId);    
    return category ? category.title : 'Nessuna categoria';
  }

  // Cateroty Modal
  openAddProductModal(): void {
    this.isAddProductModalOpen = true;
    document.body.classList.add('modal-open');
  }

  closeAddProductModal(): void {
    this.isAddProductModalOpen = false;
    this.isCreatingCategory = false;
    document.body.classList.remove('modal-open');
  }

  toggleCategoryCreation(): void {
    this.isCreatingCategory = !this.isCreatingCategory;
  }

  openAddCategoryModal(): void {
    this.isAddCategoryModalOpen = true;
    document.body.classList.add('modal-open');
  }

  closeAddCategoryModal(): void {
    this.isAddCategoryModalOpen = false;
    document.body.classList.remove('modal-open');
  }

  createCategory(): void {
    if (this.newCategory.title) {
      this.categoryService.createCategory(this.newCategory).subscribe(() => {
        this.loadCategories(); // Ricarica le categorie dopo la creazione
        this.loadProductCountByCategory();
        this.newCategory = {}; // Resetta il form
        this.isCreatingCategory = false;
        this.closeAddCategoryModal()
      });
    }
  }

  openUpdateCategoryModal(category: any): void {
    this.selectedCategory = { ...category }; 
    this.isUpdateCategoryModalOpen = true;
    document.body.classList.add('modal-open');
  }
  
  // Chiudi il modal di aggiornamento
  closeUpdateCategoryModal(): void {
    this.isUpdateCategoryModalOpen = false;
    document.body.classList.remove('modal-open');
  }
  
  // Submit per l'aggiornamento della categoria
  onUpdateCategorySubmit(): void {
    if (this.selectedCategory.title) {
      console.log(this.selectedCategory.title);
      this.categoryService.updateCategory(this.selectedCategory.sequId, this.selectedCategory.title).subscribe(() => {
        this.loadCategories(); // Ricarica le categorie dopo l'aggiornamento
        this.loadProducts();
        this.closeUpdateCategoryModal(); // Chiudi il modal
      });
    }
  }
  
  
  getProductCount(categoryId: number): number {
    return this.productCountByCategory.get(categoryId) || 0;
  }
  


  // Products Modal add, modify

  onSubmit(): void {
    const productToSend = {
      title: this.newProduct.title,
      description: this.newProduct.description,
      imgResources: this.newProduct.imgResources,
      imgResources2: this.newProduct.imgResources2,
      imgResources3: this.newProduct.imgResources3,
      price: this.newProduct.price,
      stock: parseInt(this.newProduct.stock),
      categoryId: parseInt(this.newProduct.categoryId)
    };
  
    this.productService.createProduct(productToSend).subscribe(() => {
      this.loadProducts(); 
      this.loadProductCountByCategory();
      this.closeAddProductModal(); 
      this.newProduct = {}; 
    });
  }

  
  openUpdateProductModal(product: any): void {
    this.selectedProduct = { ...product };    
    this.isUpdateProductModalOpen = true;
    document.body.classList.add('modal-open');
  }

  
  closeUpdateProductModal(): void {
    this.isUpdateProductModalOpen = false;
    this.isCreatingCategory = false;
    document.body.classList.remove('modal-open');
  }

  
  onUpdateSubmit(): void {
    const productToUpdate = {
      title: this.selectedProduct.title,
      description: this.selectedProduct.description,
      imgResources: this.selectedProduct.imgResources,
      imgResources2: this.selectedProduct.imgResources2,
      imgResources3: this.selectedProduct.imgResources3,
      price: this.selectedProduct.price,
      stock: parseInt(this.selectedProduct.stock),
      categoryId: parseInt(this.selectedProduct.categoryId)
    };
  
    this.productService.updateProduct(this.selectedProduct.sequId, productToUpdate).subscribe(() => {
      this.loadProducts(); 
      this.loadProductCountByCategory();
      this.closeUpdateProductModal(); 
    });
  }

  

updateImagePreview(field: string, event: Event): void {
  const inputElement = event.target as HTMLInputElement;
  const value = inputElement.value;

  switch (field) {
    case 'imgResources':
      this.selectedProduct.imgResources = value;
      break;
    case 'imgResources2':
      this.selectedProduct.imgResources2 = value;
      break;
    case 'imgResources3':
      this.selectedProduct.imgResources3 = value;
      break;
  }
}


// Order Box

loadOrders(page: number = this.orderCurrentPage): void {
  const size = this.orderPageSize;
  let params = new HttpParams()
    .set('page', page.toString())
    .set('size', size.toString())
    .set('sort', `${this.orderSortBy},${this.orderSortOrder}`);

  
  if (this.selectedStatus) params = params.set('status', this.selectedStatus);  
  if (this.orderSearchEmail) params = params.set('searchEmail', this.orderSearchEmail);
  if (this.dateRange.start) {
    const startUtc = new Date(this.dateRange.start.getTime() - (this.dateRange.start.getTimezoneOffset() * 60000));
    params = params.set('startDate', startUtc.toISOString().split('T')[0]);
  }
  if (this.dateRange.end) {
    const endUtc = new Date(this.dateRange.end.getTime() - (this.dateRange.end.getTimezoneOffset() * 60000));
    params = params.set('endDate', endUtc.toISOString().split('T')[0]);
  }

  this.orderService.getAllOrdersPaged(params).subscribe((response: any) => {
    this.orders = response.content.map((order: Order) => ({
      sequId: order.sequId,
      status: order.status,
      userEmail: order.user.email,
      productCount: order.items.length,
      totalPrice: order.totalPrice,
      createdAt: order.createdAt
    }));
    this.orderTotalItems = response.totalElements;
  });
}


resetOrderFilters(): void {
  this.selectedStatus = '';
  this.startDate = null;
  this.endDate = null;
  this.dateRange = { start: null, end: null };
  this.orderSearchEmail = '';
  this.orderSortBy = 'createdAt';
  this.orderSortOrder = 'desc';
  this.orderCurrentPage = 0;
  this.loadOrders();
}


onOrderSortChange(field: string): void {
  if (this.orderSortBy === field) {
    this.orderSortOrder = this.orderSortOrder === 'asc' ? 'desc' : 'asc';
  } else {
    this.orderSortBy = field;
    this.orderSortOrder = 'asc';
  }
  this.loadOrders(0);
}


applyOrderFilters(): void {
  this.orderCurrentPage = 0;
  this.loadOrders();
}


onOrderPageChange(page: number): void {
    this.orderCurrentPage = page;
    this.loadOrders(page);
  }

updateOrderStatus(orderId: number, status: string): void {
  this.orderService.updateOrderStatus(orderId, status).subscribe(() => {
    this.loadOrders(this.currentPage); 
  });
}

openUpdateOrderModal(order: Order): void {
  this.selectedOrder = { ...order };
  this.newOrderStatus = this.selectedOrder.status;
  this.isUpdateOrderModalOpen = true;
  document.body.classList.add('modal-open');
}

closeUpdateOrderModal(): void {
  this.isUpdateOrderModalOpen = false;
  this.newOrderStatus = this.selectedOrder.status;
  document.body.classList.remove('modal-open');
}

onUpdateOrderStatusSubmit(): void {
  if (!this.newOrderStatus) {
    console.error('Seleziona uno stato!');
    return;
  }
  
  
  this.updateOrderStatus(this.selectedOrder.sequId, this.newOrderStatus);
  
  
  this.closeUpdateOrderModal();
}


viewOrderDetails(orderId: number): void {
  
  const path = `/order-details/${orderId}`;
  const url = this.location.prepareExternalUrl(path);  
  window.open(url, '_blank');
}

viewProductDetails(productId: number): void {
  
  const path = `/shop-details/${productId}`;
  const url = this.location.prepareExternalUrl(path);
  window.open(url, '_blank');
}


// Counter Box

userCount():void{
  this.userService.getTotalUser().subscribe(
    (data) => {
        this.countUser = data;
        
    },
    (error) => {
        console.error(error);
    }
);
}

totalCountOrders():void{
  this.orderService.getCountConfirmedOrders().subscribe(
    (data) => {
        this.countOrders = data;
        
    },
    (error) => {
        console.error(error);
    }
);
}

totalRevenueOrders():void{
  this.orderService.getTotalRevenue().subscribe(
    (data) => {
        this.totalRevenue = data;
        
    },
    (error) => {
        console.error(error);
    }
);
}

totalCountProducts():void{
  this.productService.getCountProducts().subscribe(
    (data) => {
        this.counProducts = data;
        
    },
    (error) => {
        console.error(error);
    }
);
}

totalCountCategories():void{
  this.categoryService.getTotalCount().subscribe(
    (data) => {
        this.countCategories = data;
        
    },
    (error) => {
        console.error(error);
    }
);
}

// Order count, revenue graph

loadSalesData(): void {
  this.orderService.getDailySales().subscribe(data => {
    const labels = data.map((item: { date: any; }) => item.date);
    const totals = data.map((item: { total: number; }) => Number(item.total.toFixed(2)));
    const counts = data.map((item: { count: any; }) => item.count);

    this.chartData = {
      labels: labels,
      datasets: [
        {
          label: 'Daily Sales',
          data: totals,
          fill: false,
          borderColor: '#59ab6e',
          backgroundColor: 'rgba(89, 171, 110, 0.2)',
          tension: 0.4,
          yAxisID: 'y'
        },
        {
          label: 'Order Count',
          data: counts,
          fill: false,
          borderColor: '#FF5733',
          backgroundColor: 'rgba(255, 87, 51, 0.2)',
          borderDash: [5, 5],
          tension: 0.4,
          yAxisID: 'y1'
        }
      ]
    };

    this.chartOptions = {
      responsive: true,
      plugins: {
        legend: {
          position: 'top',
          display: true,
        },
        tooltip: {
          callbacks: {
            label: function (tooltipItem: any) {
              const datasetLabel = tooltipItem.dataset.label || '';
              const value = tooltipItem.raw;
              if (datasetLabel === 'Daily Sales') {
                return `Sales: $${value}`;
              } else if (datasetLabel === 'Order Count') {
                return `Orders: ${value}`;
              }
              return `${datasetLabel}: ${value}`;
            }
          }
        }
      },
      scales: {
        x: {
          title: {
            display: true,
            text: 'Date'
          }
        },
        y: {
          title: {
            display: true,
            text: 'Total Sales'
          },
          beginAtZero: true,
          position: 'left',
        },
        y1: {
          title: {
            display: true,
            text: 'Order Count'
          },
          beginAtZero: true,
          position: 'right',
          grid: {
            drawOnChartArea: false 
          }
        }
      }
    };
  });
}




}

