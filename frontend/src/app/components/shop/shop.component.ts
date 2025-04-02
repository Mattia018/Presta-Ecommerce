import { Component,ElementRef,OnInit, Renderer2  } from '@angular/core';
import { ProductService } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { CartEventService } from '../../services/cart-event.service';
import { Product } from 'src/app/models/product';
import { CategoryService } from 'src/app/services/category.service';
import { Category } from 'src/app/models/category';
import { Options } from '@angular-slider/ngx-slider';

@Component({
  selector: 'app-shop',
  templateUrl: './shop.component.html',
  styleUrls: ['./shop.component.scss']
})
export class ShopComponent implements OnInit {
  products: Product[] = [];
  currentPage = 0;
  pageSize = 12;
  totalItems = 0;
  categories: Category[] = [];

  // Filtri
  selectedCategory!: string | '';
  minPrice: number = 0;
  maxPrice: number =2000;
  searchQuery!: string | '';
  selectedSort: string = 'newest';
  priceSliderOptions: Options = {
    floor: 0,
    ceil: 2000,
    step: 10,
    showTicks: false,
    hideLimitLabels: true,
    hidePointerLabels: false,
    translate: (value: number): string => {
      return '$' + value;
    },

    
    getPointerColor: (): string => {
      return '#59ab6e'; 
    },
    getSelectionBarColor: (): string => {
      return '#59ab6eed'; 
    },
    getTickColor: (): string => {
      return '#e9e9e9';
    },
    animate: false
  };

  
  sortBy: string = 'sequId';
  sortDirection: string = 'desc';


  constructor(
    private productService: ProductService,
    private cartService: CartService,
    private authService: AuthService,
    private router: Router,
    private cartEventService: CartEventService,
    private categoryService: CategoryService,
    private renderer: Renderer2, private el: ElementRef
  ) {}

  ngOnInit(): void {
    this.loadProducts();
    this.loadCategories();
    this.sortByNewest();
    this.cartEventService.notifyShopPageLoaded();
  }

  loadProducts(): void {
    this.productService.getAllProductsShop(
      this.currentPage,
      this.pageSize,
      this.selectedCategory || undefined,
      this.minPrice|| undefined,
      this.maxPrice|| undefined,
      this.searchQuery|| undefined,
      this.sortBy|| undefined,
      this.sortDirection|| undefined
    ).subscribe((response: any) => {
      this.products = response.content;
      this.totalItems = response.totalElements;
    });
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe(
      (categories) => {
        this.categories = categories;        
      },
      (error) => {
        console.error('Errore durante il caricamento delle categorie:', error);
      }
    );
  }


  onPageChange(event: any): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadProducts();
  }

  getPages(): number[] {
    const totalPages = Math.ceil(this.totalItems / this.pageSize);
    return Array.from({ length: totalPages }, (_, i) => i);
  }
  
  goToPage(page: number): void {
    this.currentPage = page;
    this.loadProducts();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  
  // Metodo per aggiungere un prodotto al carrello
  addToCart(productId: number,event: MouseEvent): void {
    const userId = this.authService.getCurrentUserId();
    if (userId) {
    

    // Animazione click add

    const button = event.target as HTMLElement;
    const cartIcon = document.querySelector('.cart-icon') as HTMLElement;

    const buttonRect = button.getBoundingClientRect();
    const cartRect = cartIcon.getBoundingClientRect();

    const flyIcon = this.renderer.createElement('div');
    this.renderer.addClass(flyIcon, 'fly-to-cart-icon');
    this.renderer.appendChild(flyIcon, this.renderer.createText('🛒'));
    this.renderer.appendChild(document.body, flyIcon);

    this.renderer.setStyle(flyIcon, 'position', 'fixed');
    this.renderer.setStyle(flyIcon, 'left', `${buttonRect.left + buttonRect.width / 2}px`);
    this.renderer.setStyle(flyIcon, 'top', `${buttonRect.top + buttonRect.height / 2}px`);

    setTimeout(() => {
      this.renderer.setStyle(flyIcon, 'transition', 'all 0.5s ease-in-out');
      this.renderer.setStyle(flyIcon, 'left', `${cartRect.left + cartRect.width / 2}px`);
      this.renderer.setStyle(flyIcon, 'top', `${cartRect.top + cartRect.height / 2}px`);
      this.renderer.setStyle(flyIcon, 'opacity', '0');
      this.renderer.setStyle(flyIcon, 'transform', 'scale(0.5)');

      setTimeout(() => {
        this.renderer.removeChild(document.body, flyIcon);
      }, 500);
    }, 0);


    // Add Product

      this.cartService.addProductToCart(userId, productId, 1).subscribe({
        next: () => {
          
        },
        error: (err) => {
          console.error('Errore durante l\'aggiunta al carrello:', err);
          alert('Si è verificato un errore durante l\'aggiunta al carrello.');
        }
      });
    } else {
      // Se l'utente non è loggato, reindirizzalo alla pagina di login
      this.router.navigate(['/login']);
    }
  }

  // Filri ed Ordinamento

  goToProductDetails(productId: number): void {
    this.router.navigate(['/shop-details', productId]);
  }
  
  applyCategoryFilter(category: string): void {
    this.selectedCategory = category;
    this.currentPage = 0;
    this.loadProducts();
  }

  applyPriceFilter(minPrice: number , maxPrice: number ): void {
    this.minPrice = minPrice;
    this.maxPrice = maxPrice;
    this.currentPage = 0;
    this.loadProducts();
  }

  applySearchFilter(searchQuery: string): void {
    this.searchQuery = searchQuery || '';
    this.currentPage = 0;
    this.loadProducts();
  }

  
  sortByPriceLowToHigh(): void {
    this.sortBy = 'price';
    this.sortDirection = 'asc';
    this.loadProducts();
  }

  sortByPriceHighToLow(): void {
    this.sortBy = 'price';
    this.sortDirection = 'desc';
    this.loadProducts();
  }

  sortByNewest(): void {
    this.sortBy = 'sequId';
    this.sortDirection = 'desc';
    this.loadProducts();
  }

  resetFilters(): void {    
    this.selectedCategory = '';
    this.minPrice = 0;
    this.maxPrice = 5000;
    this.searchQuery = '';
    this.sortBy = 'sequId';
    this.sortDirection = 'desc';
    this.selectedSort='newest';    
    this.currentPage = 0;

    
    this.loadProducts();
    this.loadCategories();
  }

  applySorting() {
    switch(this.selectedSort) {
      case 'price-low':
        this.sortByPriceLowToHigh();
        break;
      case 'price-high':
        this.sortByPriceHighToLow();
        break;
      case 'newest':
        this.sortByNewest();
        break;
      default:
        
        break;
    }
  }

}
