from django.db import models
class student_info(models.Model):
	student_name = models.CharField(max_length = 100)
	student_age = models.IntegerField(default = 0)
	student_gender = models.CharField(max_length = 10)
	student_major = models.CharField(max_length = 100)

# Create your models here.
