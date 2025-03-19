import { Component } from '@angular/core';
import { ApiService } from './service/http.service';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import {IRequestResponse} from './interfaces/request.interface'


@Component({
  selector: 'app-root',
  imports: [CommonModule,ReactiveFormsModule],
  providers: [ApiService,FormControl],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
  standalone:true
})
export class AppComponent {
  data:IRequestResponse[] = [];
  loading:boolean = false;
  unStructuredData = new FormControl('');
  response:string = '';

  constructor(private apiService: ApiService) {}


  ngOnInit(): void {
    console.log('ngOnInit executed');
    this.fetchData();
  }

  fetchData() {
    this.apiService.getData<IRequestResponse[]>('/all').subscribe({
      next: (response) => {
        this.data = response;
        console.log('Data received:', response);
      },
      error: (error) => {
        console.error('Error fetching data:', error);
      }
    });
  }


  submit() {
    const requestData = { unStructuredData: this.unStructuredData.value };
    this.apiService.postData('/sent',requestData).subscribe({
      next: (response) => {
        this.response = response?.data;
        console.log('Data received:', response.data);
      },
      error: (error) => {
        console.error('Error fetching data:', error);
      }
    });
  }
}
